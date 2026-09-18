# V6 Real Call / Video Call

- WebRTC audio/video əlavə edildi.
- call_start / call_incoming / call_signal / call_poll signaling.
- Mikrofon, speaker, camera on/off, camera switch, accept/reject/end.
- STUN default. TURN server konfiqurasiyası `ice_config.php` vasitəsilə gəlir.
- FCM hələ yoxdur; incoming call ChatActivity açıq/aktiv olduqda polling ilə görünür.
- Stabil real-world zəng üçün TURN quraşdırılması tövsiyə olunur.
