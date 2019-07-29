package com.glencoesoftware.omero.ms.core;

import org.slf4j.LoggerFactory;

import brave.handler.FinishedSpanHandler;
import brave.handler.MutableSpan;
import brave.propagation.TraceContext;

import io.prometheus.client.Summary;


public class PrometheusSpanHandler extends FinishedSpanHandler {

    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(PrometheusSpanHandler.class);

    private Summary spanDuration;

    public PrometheusSpanHandler() {
        spanDuration = Summary.build()
                .name("spanDuration")
                .labelNames("spanName")
                .help("The duration of spans")
                .register();
    }

    /**
     * This is invoked after a span is finished, allowing data to be modified or reported out of
     * process. A return value of false means the span should be dropped completely from the stream.
     *
     * <p>Changes to the input span are visible by later finished span handlers. One reason to change the
     * input is to align tags, so that correlation occurs. For example, some may clean the tag
     * "http.path" knowing downstream handlers such as zipkin reporting have the same value.
     *
     * <p>Returning false is the same effect as if {@link Span#abandon()} was called. Implementations
     * should be careful when returning false as it can lead to broken traces. Acceptable use cases
     * are when the span is a leaf, for example a client call to an uninstrumented database, or a
     * server call which is known to terminate in-process (for example, health-checks). Prefer an
     * instrumentation policy approach to this mechanism as it results in less overhead.
     *
     * <p>Implementations should not hold a reference to it after this method returns. This is to
     * allow object recycling.
     *
     * @param context the trace context which is {@link TraceContext#sampled()} or {@link
     * TraceContext#sampledLocal()}. This includes identifiers and potentially {@link
     * TraceContext#extra() extra propagated data} such as extended sampling configuration.
     * @param span a mutable object including all data recorded with span apis. Modifications are
     * visible to later handlers, including Zipkin.
     * @return true retains the span, and should almost always be used. false drops the span, making
     * it invisible to later handlers such as Zipkin.
     */
    @Override
    public boolean handle(TraceContext context, MutableSpan span) {
        spanDuration.labels(span.name()).observe(span.finishTimestamp() - span.startTimestamp());
        log.info(span.name());
        log.info("In FinishedSpanHandler");
        return true;
    }

}
