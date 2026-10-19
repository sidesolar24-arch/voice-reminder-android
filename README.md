# Voice Reminder Android App

Bu proje, Android için tarih ve saat ayarlı sesli hatırlatıcı uygulamasının kaynak kodunu içerir. Kullanıcı adı ve soyadı girilip, hatırlatma mesajı yazılabilir, kategori/renk atanabilir, haftanın günleri seçilebilir, birden fazla saat eklenebilir ve melodi (veya kendi ses dosyası) seçilebilir.

## Özellikler

- Kullanıcı adı / soyadı girişi, hatırlatma mesajı metin girişi
- Haftanın günlerini seçme, aynı gün içinde birden fazla saat ekleme
- Kayıtlı bir hatırlatıcıyı sonradan **düzenleme** (yalnızca ekle/sil değil)
- **Kategori** etiketi (İş / Sağlık / Kişisel / Diğer) — renkli rozet, ana ekranda kategoriye göre filtreleme
- **Arama** kutusu (isim/mesaj içinde, Türkçe ı/i duyarlı normalize ile)
- Ana ekranda **"Bugün"** özet kartı: bugünün hatırlatıcı sayısı ve sıradaki hatırlatıcı
- Üç hazır melodi **veya cihazdan kendi ses dosyanızı seçme**
- Hatırlatma tetiklendiğinde 1 dakika süreyle alarm sesi çalması ve mesajın Türkçe TTS ile okunması
- Bildirimde **"Durdur"** ve **"Ertele (10 dk)"** aksiyonları
- Cihaz yeniden başlatıldığında kayıtlı hatırlatıcıların yeniden planlanması
- Material 3 tema: karanlık mod desteği, Android 12+ cihazlarda dinamik (duvar kağıdına uyumlu) renk şeması

## Kurulum

1. Android Studio ile bu projeyi açın.
2. Gerekirse `File > Sync Project with Gradle Files` seçeneğini kullanın.
3. Uygulamayı bir cihaz veya emülatör üzerinde çalıştırın.

Komut satırından derlemek için: bkz. `~/.gradle/gradle.properties` (bu makinede `org.gradle.java.home` JDK 17'ye ayarlı — sistemdeki varsayılan `java` sürüm 8, Android Gradle Plugin en az 11 istiyor).

## Notlar

- Arayüz `app/src/main/kotlin/com/example/reminder/ui/` altında `HomeScreen` (liste + bugün özeti + arama/filtre) ve `FormScreen` (ekle/düzenle ortak form) olarak iki ekrana bölündü; aralarında Compose Navigation kullanılıyor. Tema `ui/theme/` içinde.
- Hatırlatıcı kayıtları cihazda `SharedPreferences` içinde JSON olarak saklanır; eski kayıtlar (kategori/özel ses alanı olmadan) geriye dönük uyumlu okunur.
- `ReminderPlayerService` arka planda alarm zilini (veya seçilen özel ses dosyasını) çalar, TTS mesajını okur, "Ertele" aksiyonunda `ReminderScheduler.scheduleSnooze` ile tek seferlik yeni bir alarm kurar.
- Özel ses dosyası seçimi Storage Access Framework (`OpenDocument`) ile yapılır ve kalıcı okuma izni alınır.
