package com.limelight;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.media.AudioManager;
import android.content.Context;
import android.view.InputDevice;

import java.util.Arrays;
import java.util.List;

public class KeyboardAccessibilityService extends AccessibilityService {
    
    // 블랙리스트에는 전원 버튼만 남깁니다.
    private final static List<Integer> BLACKLISTED_KEYS = Arrays.asList(KeyEvent.KEYCODE_POWER);

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        // [핵심] Alt 키가 눌리면 안드로이드 시스템에 넘기지 말고 윈도우로 '고정'해서 쏩니다.
        if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
            if (Game.instance != null && Game.instance.isConnected()) {
                if (action == KeyEvent.ACTION_DOWN) {
                    Game.instance.handleKeyDown(event);
                } else if (action == KeyEvent.ACTION_UP) {
                    Game.instance.handleKeyUp(event);
                }
                return true; // 여기서 true를 반환해야 안드로이드 OS가 이 신호를 꿀꺽하지 않습니다.
            }
        }

        if (Game.instance != null && Game.instance.isConnected() && Game.instance.hasWindowFocus()) {

            InputDevice device = event.getDevice();
            boolean isExternal = false;

            // 💡 핵심: 태블릿 내장 버튼과 외부 블루투스 키보드를 완벽하게 분리!
            if (device != null) {
                if (android.os.Build.VERSION.SDK_INT >= 29) {
                    isExternal = device.isExternal();
                } else {
                    isExternal = !device.getName().toLowerCase().contains("gpio") && !device.isVirtual();
                }
            }

            // [해결 1] 외부 키보드가 아닌 기기(태블릿 본체 볼륨, 전원 등)의 입력은 안드로이드로 즉시 뱉어냄!
            if (!isExternal) {
                return super.onKeyEvent(event); 
            }

            // [해결 2] Fn+F9, Fn+F10 볼륨 조절 (64번 Explorer 키코드도 예비로 추가)
            if (keyCode == 65 || keyCode == 210 || keyCode == 64) {
                if (action == KeyEvent.ACTION_DOWN) {
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    if (keyCode == 65 || keyCode == 64) { 
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                    } else if (keyCode == 210) { 
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                    }
                }
                return true; 
            }

            // 나머지 모든 블루투스 키보드 입력은 원격 PC로 전송
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
