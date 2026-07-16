package com.limelight;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import java.util.Arrays;
import java.util.List;

public class KeyboardAccessibilityService extends AccessibilityService {
    
    //아래의 블랙리스트에서 KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN 삭제됨
    private final static List<Integer> BLACKLISTED_KEYS = Arrays.asList(KeyEvent.KEYCODE_POWER);

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        // ▼▼▼ 여기에 스마트 포커스 조건문 3줄을 추가합니다 ▼▼▼
        if (!Game.isMoonlightFocused) {
            return super.onKeyEvent(event); // 가로채지 말고 안드로이드(카톡 등)로 넘겨라
        }
        // ▲▲▲ 추가 끝 ▲▲▲

        // ▼▼▼ 여기에 태블릿 본체 볼륨 버튼 살리기 코드 추가 ▼▼▼
        android.view.InputDevice device = event.getDevice();
        boolean isRealKeyboard = (device != null && device.getKeyboardType() == android.view.InputDevice.KEYBOARD_TYPE_ALPHABETIC);
        
        // 키보드가 아닌 기기(태블릿 본체)에서 누른 볼륨 버튼은 가로채지 않고 통과!
        if (!isRealKeyboard && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            return super.onKeyEvent(event); 
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
