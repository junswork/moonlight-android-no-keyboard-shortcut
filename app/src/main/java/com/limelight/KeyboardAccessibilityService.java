package com.limelight;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.media.AudioManager;
import android.content.Context;

import java.util.Arrays;
import java.util.List;

public class KeyboardAccessibilityService extends AccessibilityService {
    
    //아래의 블랙리스트에서 KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN 삭제됨
    private final static List<Integer> BLACKLISTED_KEYS = Arrays.asList(KeyEvent.KEYCODE_POWER);

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        // 1. 문라이트가 켜져 있고, 현재 '정확히 터치되어 포커스를 잡고 있을 때만' 작동!
        if (Game.instance != null && Game.instance.isConnected() && Game.instance.hasWindowFocus()) {

            android.view.InputDevice device = event.getDevice();
            boolean isRealKeyboard = (device != null && device.getKeyboardType() == android.view.InputDevice.KEYBOARD_TYPE_ALPHABETIC);

            // 2. 태블릿 본체의 진짜 볼륨 버튼은 가로채지 말고 패스
            if (!isRealKeyboard && (keyCode == 24 || keyCode == 25)) {
                return super.onKeyEvent(event); 
            }

            // 3. Fn+이메일(65) / Fn+계산기(210) ➔ 탭 볼륨 조절로 둔갑
            if (isRealKeyboard && (keyCode == 65 || keyCode == 210)) {
                if (action == KeyEvent.ACTION_DOWN) {
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    if (keyCode == 65) { 
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                    } else if (keyCode == 210) { 
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                    }
                }
                return true; // 윈도우로 안 넘어가게 여기서 신호를 꿀꺽!
            }

            // 4. 나머지 모든 키보드 입력은 원격 PC로 전송
            if (!BLACKLISTED_KEYS.contains(keyCode)) {
                if (action == KeyEvent.ACTION_DOWN) {
                    Game.instance.handleKeyDown(event);
                    return true;
                } else if (action == KeyEvent.ACTION_UP) {
                    Game.instance.handleKeyUp(event);
                    return true;
                }
            }
        }

        // 5. 문라이트 포커스 아웃 상태이거나, 위 조건에 해당하지 않으면 안드로이드(카톡 등)로 키보드 신호 정상 반환!
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
