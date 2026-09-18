V6.3 fixes
- Version 6.3.0 / code 8.
- Message local-clear filter changed from date/time to message ID to avoid timezone-based hiding of new messages.
- Old cleared_before is removed automatically.
- WebRTC calls are serialized on one dedicated HandlerThread to avoid native sequence/thread-check SIGABRT.
- Hardware AEC/NS paths are disabled; WebRTC software processing is used.
- WebRTC is initialized only when opening a call, not at app startup.
- WebRTC SDK pinned to 137.7151.05 instead of 150.7871.01 for a more established build.
