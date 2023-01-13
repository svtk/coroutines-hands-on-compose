package variant.contributors

enum class Variant {
    BLOCKING,         // Request1Blocking
    BACKGROUND,       // Request2Background
    CALLBACKS,        // Request3Callbacks
    SUSPEND,          // Request4Coroutine
    CONCURRENT,       // Request5Concurrent
    NOT_CANCELLABLE,  // Request6NotCancellable
    PROGRESS,         // Request6Progress
    CHANNELS,         // Request7Channels
    FLOW_WITH_MANUAL_CHANNEL,             // Request8Flow
    SEQUENTIAL_FLOW,
    CHANNEL_FLOW,
    NON_SUSPEND_CHANNEL_FLOW
}