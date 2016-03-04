package giorag.dailytimer.enums;

import giorag.dailytimer.activities.MainActivity;

public enum RunningState {
    Running {
        @Override
        public void restore(MainActivity activity) {
            activity.onStartClick();
        }

    },
    Paused {
        @Override
        public void restore(MainActivity activity) {
            activity.onPauseClick();
        }
    },
    Default {
        @Override
        public void restore(MainActivity activity) {
            activity.onResetClick();
        }
    };

    public abstract void restore(MainActivity activity);
}
