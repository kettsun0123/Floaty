package uno.ketts.floaty;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.transition.Scene;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by Yuji Koketsu on 2017/11/24.
 */
class FloatyManager {
    static final int MSG_TIMEOUT = 0;

    private static final int SHORT_DURATION_MS = 1500;
    private static final int LONG_DURATION_MS = 2750;

    private static FloatyManager floatyManager;

    private final Object lock;
    private final Handler handler;

    private FloatyRecord current;
    private FloatyRecord next;

    static FloatyManager getInstance() {
        if (floatyManager == null) {
            floatyManager = new FloatyManager();
        }
        return floatyManager;
    }

    private FloatyManager() {
        lock = new Object();
        handler = new android.os.Handler(Looper.getMainLooper(), new android.os.Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_TIMEOUT:
                        handleTimeout((FloatyRecord) message.obj);
                        return true;
                }
                return false;
            }
        });
    }

    interface Callback {
        void show();

        void dismiss(int event);

        void replace(Scene before);
    }

    public void show(Scene scene, int duration, Callback callback) {
        synchronized (lock) {
            if (isCurrentLocked(callback)) {
                current.duration = duration;
                handler.removeCallbacksAndMessages(current);
                scheduleTimeoutLocked(current);
                return;
            } else if (isNextLocked(callback)) {
                next.duration = duration;
            } else {
                next = new FloatyRecord(scene, duration, callback);
            }

            if (current != null && cancelLocked(current, Floaty.Callback.DISMISS_EVENT_CONSECUTIVE)) {
                return;
            } else {
                current = null;
                showNextLocked();
            }
        }
    }

    public void dismiss(Callback callback, int event) {
        synchronized (lock) {
            if (isCurrentLocked(callback)) {
                cancelLocked(current, event);
            } else if (isNextLocked(callback)) {
                cancelLocked(next, event);
            }
        }
    }

    public void replace(Scene scene, int duration, Callback callback) {
        synchronized (lock) {
            if (isCurrentLocked(callback)) {
                current.duration = duration;
                handler.removeCallbacksAndMessages(current);
                scheduleTimeoutLocked(current);
            } else {
                next = new FloatyRecord(scene, duration, callback);
            }

            if (current != null) {
                handler.removeCallbacksAndMessages(current);
                replaceToNextLocked();
            } else if (!cancelLocked(current, Floaty.Callback.DISMISS_EVENT_CONSECUTIVE)) {
                showNextLocked();
            }
        }
    }

    public void onShown(Callback callback) {
        synchronized (lock) {
            if (isCurrentLocked(callback)) {
                scheduleTimeoutLocked(current);
            }
        }
    }

    public void onDismissed(Callback callback) {
        synchronized (lock) {
            if (isCurrentLocked(callback)) {
                current = null;
                if (next != null) {
                    showNextLocked();
                } else {
                    Stage.clear();
                }
            }
        }
    }

    public void onReplaced(Callback callback) {
        synchronized (lock) {
            if (isCurrentLocked(callback)) {
                onShown(callback);
            }
        }
    }

    public boolean isCurrent(Callback callback) {
        synchronized (lock) {
            return isCurrentLocked(callback);
        }
    }

    private boolean isCurrentLocked(Callback callback) {
        return current != null && current.isFloaty(callback);
    }

    private boolean isNextLocked(Callback callback) {
        return next != null && next.isFloaty(callback);
    }

    private void showNextLocked() {
        if (next != null) {
            current = next;
            next = null;

            final Callback callback = current.callback.get();
            if (callback != null) {
                callback.show();
            } else {
                current = null;
            }
        }
    }

    private void replaceToNextLocked() {
        if (next != null) {
            final Scene scene = current.scene.get();

            final Callback callback = next.callback.get();
            if (callback != null) {
                callback.replace(scene);
                current = next;
                next = null;
            } else {
                next = null;
            }
        }
    }

    private boolean cancelLocked(FloatyRecord record, int event) {
        final Callback callback = record.callback.get();
        if (callback != null) {
            handler.removeCallbacksAndMessages(record);
            callback.dismiss(event);
            return true;
        }
        return false;
    }

    private void scheduleTimeoutLocked(FloatyRecord record) {
        if (record.duration == Floaty.LENGTH_INDEFINITE) {
            return;
        }

        int durationMs = LONG_DURATION_MS;
        if (record.duration > 0) {
            durationMs = record.duration;
        } else if (record.duration == Floaty.LENGTH_SHORT) {
            durationMs = SHORT_DURATION_MS;
        }
        handler.removeCallbacksAndMessages(record);
        handler.sendMessageDelayed(Message.obtain(handler, MSG_TIMEOUT, record), durationMs);
    }

    void handleTimeout(FloatyRecord record) {
        synchronized (lock) {
            if (current == record || next == record) {
                cancelLocked(record, Floaty.Callback.DISMISS_EVENT_TIMEOUT);
            }
        }
    }

    private static class FloatyRecord {
        final WeakReference<Callback> callback;
        final WeakReference<Scene> scene;
        int duration;
        boolean paused;

        public FloatyRecord(Scene scene, int duration, Callback callback) {
            this.scene = new WeakReference<>(scene);
            this.duration = duration;
            this.callback = new WeakReference<>(callback);
        }

        boolean isFloaty(Callback callback) {
            return callback != null && this.callback.get() == callback;
        }
    }
}