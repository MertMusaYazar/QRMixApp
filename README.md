# QRMixApp 📱🔍

QRMixApp, Kotlin diliyle yerel (Native) olarak geliştirilmiş; standart statik QR kodları üretebilen ve tarayabilen, bunun da ötesinde Firebase Firestore entegrasyonuyla uzaktan yönetilebilir **"Dinamik QR Kodlar"** oluşturan gelişmiş bir Android uygulamasıdır.

Uygulama; basılmış veya paylaşılmış bir QR kodun içeriğinin, **herhangi bir fiziksel değişiklik gerektirmeden, şifre korumalı bir şekilde uzaktan anlık olarak güncellenmesine** olanak tanır.

---

## 🔥 Öne Çıkan Gelişmiş Özellikler

### 1. Akıllı Filtreleme ve Dinamik QR Algılama
* **Gelişmiş Kamera Entegrasyonu:** `CodeScanner` kütüphanesi ile cihaz kamerası üzerinden gerçek zamanlı, gecikmesiz karekod taraması yapılır.
* **Özel Protokol Filtresi:** Taranan QR kodun içeriği `MainActivity` içinde anlık analiz edilir. Eğer içerik `mertdynamicqr52-` veya `mertdynamicqr(52)-` gibi özel ön eklerle (prefix) başlıyorsa, uygulama bunun bir dinamik QR olduğunu anlar ve doğrudan Firebase sorgu ekranına yönlendirir. Standart içerikler ise doğrudan panoya kopyalanabilir.

### 2. İki Farklı Dinamik Veri Modeli
* **Dinamik Metin Modu:** Uzaktan değiştirilebilir, tek parça düz metin içeriklerini barındırır.
* **Dinamik Tablo Modu (`Dynamic Table Layout`):** Uygulama içinde tamamen dinamik olarak satır ve sütun eklenebilen gelişmiş bir tablodur. Satır ve sütun verileri veri tabanında kaybolmasın diye özel bir string serileştirme algoritması (`row1_col1,row1_col2;row2_col1...`) ile sıkıştırılarak Firestore'a tek bir alan olarak kaydedilir ve okunurken tekrar matrise dönüştürülür.

### 3. Kilitli Güvenlik Mekanizması (Şifre Koruması)
* Dinamik QR kodların yetkisiz kişilerce değiştirilmesini önlemek amacıyla **6 haneli özel bir şifreleme katmanı** bulunur.
* Veri tabanından çekilen belgenin şifresi çözülmeden arayüzdeki düzenleme alanları (`EditText`'ler, satır/sütun ekleme butonları) tamamen **kilitli (disabled)** ve salt okunur modda kalır.
* Kullanıcı doğru şifreyi girip "Kilidi Aç" butonuna bastığında arayüz aktifleşir ve güncelleme yetkisi verilir.

### 4. Çevrimdışı Çalışma Desteği (Offline Persistence)
* Firebase Firestore altyapısı `PersistenceEnabled = true` olarak yapılandırılmıştır. Bu sayede daha önce taranan dinamik QR verileri cihaz hafızasına önbelleklenir; internet bağlantısı tamamen kopsa bile kullanıcı karekodu tarattığında güncel verileri internetsiz ortamda da görebilir.

<p align="center">
  <img src="https://github.com/user-attachments/assets/be05db99-2b25-4cd6-9df7-830c92da342b" width="22%" alt="Ana Ekran" style="margin-right: 5px;">
  <img src="https://github.com/user-attachments/assets/05ee3d18-652b-420d-a96e-9cbdc5daabf7" width="22%" alt="Şifre Kilitli Ekran" style="margin-right: 5px;">
  <img src="https://github.com/user-attachments/assets/d9781a6e-1c77-472c-9ffc-be71dc743e6b" width="22%" alt="Dinamik Metin Düzenleme" style="margin-right: 5px;">
  <img src="https://github.com/user-attachments/assets/3b6d8221-bea0-441d-a271-90ca340061eb" width="22%" alt="Dinamik Tablo Düzenleme">
</p>


---

## 🛠️ Teknik Mimari ve Kullanılan Teknolojiler

* **Minimum SDK:** 24 (Android 7.0 Nougat ve üzeri)
* **Dil & Ekosistem:** Kotlin, Jetpack (Saf Android)
* **Veri Tabanı:** Firebase Firestore (NoSQL Bulut Veri Tabanı) 
* **QR Kütüphaneleri:** * `com.github.budiyev:code-scanner` (Kamera tarama motoru) 
  * `com.google.zxing:core` (QR Kod oluşturma motoru) 

---

## 📂 Detaylı Kod Yapısı ve Sınıf Görevleri

### 📄 `MainActivity.kt`
* Uygulamanın giriş kapısıdır. `CodeScanner` kütüphanesini yönetir, kamera izinlerini kontrol eder. Taranan verinin standart bir metin mi yoksa buluttan çekilecek dinamik bir kod mu olduğunu ayırt eden regex/filtreleme mantığı burada çalışır.

### 📄 `DynamicQrActivity.kt`
* Uygulamanın en karmaşık ve detaylı sınıfıdır. 
* **Veri Yönetimi:** Firestore'daki `dynamic_qrs` koleksiyonuna bağlanarak ilgili dökümanı gerçek zamanlı dinler.
* **Şifre Algoritması:** Kullanıcının girdiği şifreyi veri tabanındaki `password` alanı ile eşleştirerek arayüzün `isEnabled` durumunu yönetir.
* **Dinamik Arayüz:** Tablo modunda, XML tarafındaki `TableLayout` içerisine Kotlin koduyla dinamik olarak `TableRow` ve `EditText` elemanları enjekte eder. Verileri virgül (`,`) ve noktalı virgül (`;`) işaretlerine göre parçalayarak tabloya doldurur veya tam tersi şekilde paketleyip Firestore'a günceller.

### 📄 `QrGeneratorActivity.kt`
* Kullanıcının girdiği herhangi bir metni veya bağlantıyı anlık olarak `ZXing` kütüphanesi aracılığıyla işler, bir `Bitmap` grafiğine dönüştürür ve ekrandaki `ImageView` üzerinde statik QR kod olarak gösterir.

### 📄 `InfoActivity.kt`
* Uygulamanın versiyonlama, geliştirici bilgileri ve kurumsal detaylarını şık bir arayüzle kullanıcıya sunan bilgi ekranıdır.

---

## ⚙️ Kurulum ve Yapılandırma

1. Projeyi klonlayın:
   ```bash
   git clone [https://github.com/MertMusaYazar/QRMixApp.git](https://github.com/MertMusaYazar/QRMixApp.git)![Uploading Screenshot_20260711_150206.png…]()
