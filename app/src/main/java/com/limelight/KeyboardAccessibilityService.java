package com.limelight;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import java.util.Arrays;
import java.util.List;

public class KeyboardAccessibilityService extends AccessibilityService {

    private final static List<Integer> BLACKLISTED_KEYS = Arrays.asList(KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_POWER);

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        // ▼▼▼ 여기에 스마트 포커스 조건문 3줄을 추가합니다 ▼▼▼
        if (!Game.isMoonlightFocused) {
            return super.onKeyEvent(event); // 가로채지 말고 안드로이드(카톡 등)로 넘겨라
        }
        // ▲▲▲ 추가 끝 ▲▲▲
        
        if (Game.instance != null && Game.instance.isConnected() && !BLACKLISTED_KEYS.contains(keyCode)) {
            // Preventing default will disable shortcut actions like alt+tab and etc.
            if (action == KeyEvent.ACTION_DOWN) {
                Game.instance.handleKeyDown(event);
                return true;
            } else if (action == KeyEvent.ACTION_UP) {
                Game.instance.handleKeyUp(event);
                return true;
            }
        }

        return super.onKeyEvent(event);
    }

    @Override
    public void onServiceConnected() {
        LimeLog.info("Keyboard service is connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }
}
