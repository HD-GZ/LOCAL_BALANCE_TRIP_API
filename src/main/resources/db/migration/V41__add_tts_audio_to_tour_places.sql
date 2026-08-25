ALTER TABLE tour_places
    ADD COLUMN tts_audio_key VARCHAR(500) NULL AFTER odii_theme_id,
    ADD COLUMN tts_synced_at TIMESTAMP NULL AFTER tts_audio_key;
