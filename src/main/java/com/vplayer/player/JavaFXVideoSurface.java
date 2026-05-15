package com.vplayer.player;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class JavaFXVideoSurface extends CallbackVideoSurface {

    public JavaFXVideoSurface(ImageView imageView) {
        this(new JavaFXRenderCallback(imageView));
    }

    private JavaFXVideoSurface(JavaFXRenderCallback renderCallback) {
        super(new JavaFXBufferFormatCallback(renderCallback), renderCallback, true, null);
    }

    private static class JavaFXBufferFormatCallback implements BufferFormatCallback {
        private final JavaFXRenderCallback renderCallback;

        public JavaFXBufferFormatCallback(JavaFXRenderCallback renderCallback) {
            this.renderCallback = renderCallback;
        }

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            // Store dimensions for later use when the native buffer arrives
            renderCallback.setDimensions(sourceWidth, sourceHeight);
            return new RV32BufferFormat(sourceWidth, sourceHeight);
        }

        @Override
        public void allocatedBuffers(ByteBuffer[] buffers) {
            // VLC has allocated its native buffer. We wrap our PixelBuffer
            // around it directly — zero copy. This must complete before
            // VLC starts calling display(), so we block with runAndWait.
            renderCallback.setupWithNativeBuffer(buffers[0]);
        }
    }

    private static class JavaFXRenderCallback implements RenderCallback {
        private final ImageView imageView;
        private volatile PixelBuffer<ByteBuffer> pixelBuffer;
        private volatile int videoWidth;
        private volatile int videoHeight;

        // Dirty flag: prevents Platform.runLater() flooding.
        // When VLC produces frames faster than JavaFX can paint,
        // extra frames are skipped instead of queuing up.
        private final AtomicBoolean needsUpdate = new AtomicBoolean(false);

        public JavaFXRenderCallback(ImageView imageView) {
            this.imageView = imageView;
        }

        public void setDimensions(int width, int height) {
            this.videoWidth = width;
            this.videoHeight = height;
        }

        /**
         * Wraps VLC's native buffer in a PixelBuffer and connects it
         * to the ImageView. Called from VLC's thread after buffer allocation.
         * We block until the FX thread is done to avoid a race.
         */
        public void setupWithNativeBuffer(ByteBuffer nativeBuffer) {
            final int w = videoWidth;
            final int h = videoHeight;

            // Use runAndWait semantics via CountDownLatch
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    WritablePixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteBgraPreInstance();
                    PixelBuffer<ByteBuffer> pb = new PixelBuffer<>(w, h, nativeBuffer, pixelFormat);
                    WritableImage writableImage = new WritableImage(pb);
                    imageView.setImage(writableImage);
                    this.pixelBuffer = pb;
                } finally {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
            PixelBuffer<ByteBuffer> pb = this.pixelBuffer;
            if (pb == null) return;

            // Only schedule an FX update if one isn't already pending.
            // This prevents flooding the FX thread — if VLC renders at 60fps
            // but FX can only repaint at 30fps, we skip the extra frames
            // instead of queuing them up (which causes lag and freeze).
            if (needsUpdate.compareAndSet(false, true)) {
                Platform.runLater(() -> {
                    needsUpdate.set(false);
                    pb.updateBuffer(pixBuf -> null);
                });
            }
        }
    }
}
