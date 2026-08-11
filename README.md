# Voice Reminder Android App

Bu proje, Android için tarih ve saat ayarlı sesli hatırlatıcı uygulamasının temel kaynak kodunu içerir. Kullanıcı adı ve soyadı girilip, hatırlatma mesajı yazılabilir, haftanın günleri seçilebilir, birden fazla saat eklenebilir ve melodiler arasından seçim yapılabilir.

## Özellikler

- Kullanıcı adı / soyadı girişi
- Hatırlatma mesajı metin girişi
- Haftanın günlerini seçme
- Aynı gün içinde birden fazla hatırlatma saati ekleme
- Üç farklı melodi seçeneği
- Hatırlatma tetiklendiğinde 1 dakika süreyle alarm sesi çalması
- Hatırlatma mesajının Türkçe Text-to-Speech olarak okunması
- Bildirimde "Durdur" düğmesiyle hatırlatmanın anında durdurulması
- Cihaz yeniden başlatıldığında kayıtlı hatırlatıcıların yeniden planlanması

## Kurulum

1. Android Studio ile bu projeyi açın.
2. Gerekirse `File > Sync Project with Gradle Files` seçeneğini kullanın.
3. Uygulamayı bir cihaz veya emülatör üzerinde çalıştırın.

## Notlar

- Proje, `app` modülü içinde Compose tabanlı arayüz ve `AlarmManager` tabanlı zamanlama içerir.
- Hatırlatıcı kayıtları cihazda `SharedPreferences` içinde JSON olarak saklanır.
- `ReminderPlayerService` arka planda alarm zilini çalar ve TTS mesajını okur.
