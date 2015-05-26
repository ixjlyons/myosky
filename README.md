# Myo Sky

A fork of [theplanethatcouldntflygood][tptcfg], which is itself a simple
Flappy Bird clone written with libgdx.

It uses the microphone as an input to the plane, allowing for myoelectric
control if an EMG sensor is plugged in to the microphone port (you could blow
or hum into the microphone for control otherwise). The signal is recorded at
8 kHz and windowed into 75 ms segments. For each segment, the RMS is calculated
and put through a moving average filter to get the input value. This input
value is used to control the vertical speed of the plane, working against
gravity.

Because the AudioRecorder class is needed, this game doesn't have an `html`
version (see [here][audiorecorder]).


[tptcfg]: https://github.com/badlogic/theplanethatcouldntflygood
[audiorecorder]: https://github.com/libgdx/libgdx/wiki/Recording-pcm-audio
