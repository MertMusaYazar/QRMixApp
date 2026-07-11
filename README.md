# QRMixApp 📱🔍

[cite_start]QRMixApp, Kotlin diliyle yerel (Native) olarak geliştirilmiş; standart statik QR kodları üretebilen ve tarayabilen, bunun da ötesinde Firebase Firestore entegrasyonuyla uzaktan yönetilebilir **"Dinamik QR Kodlar"** oluşturan gelişmiş bir Android uygulamasıdır.

[cite_start]Uygulama; basılmış veya paylaşılmış bir QR kodun içeriğinin, **herhangi bir fiziksel değişiklik gerektirmeden, şifre korumalı bir şekilde uzaktan anlık olarak güncellenmesine** olanak tanır.

---

## 🔥 Öne Çıkan Gelişmiş Özellikler

### 1. Akıllı Filtreleme ve Dinamik QR Algılama
* [cite_start]**Gelişmiş Kamera Entegrasyonu:** `CodeScanner` kütüphanesi ile cihaz kamerası üzerinden gerçek zamanlı, gecikmesiz karekod taraması yapılır.
* [cite_start]**Özel Protokol Filtresi:** Taranan QR kodun içeriği `MainActivity` içinde anlık analiz edilir. [cite_start]Eğer içerik `mertdynamicqr52-` veya `mertdynamicqr(52)-` gibi özel ön eklerle (prefix) başlıyorsa, uygulama bunun bir dinamik QR olduğunu anlar ve doğrudan Firebase sorgu ekranına yönlendirir. [cite_start]Standart içerikler ise doğrudan panoya kopyalanabilir.

### 2. İki Farklı Dinamik Veri Modeli
* [cite_start]**Dinamik Metin Modu:** Uzaktan değiştirilebilir, tek parça düz metin içeriklerini barındırır.
* [cite_start]**Dinamik Tablo Modu (`Dynamic Table Layout`):** Uygulama içinde tamamen dinamik olarak satır ve sütun eklenebilen gelişmiş bir tablodur. [cite_start]Satır ve sütun verileri veri tabanında kaybolmasın diye özel bir string serileştirme algoritması (`row1_col1,row1_col2;row2_col1...`) ile sıkıştırılarak Firestore'a tek bir alan olarak kaydedilir ve okunurken tekrar matrise dönüştürülür.

### 3. Kilitli Güvenlik Mekanizması (Şifre Koruması)
* [cite_start]Dinamik QR kodların yetkisiz kişilerce değiştirilmesini önlemek amacıyla **6 haneli özel bir şifreleme katmanı** bulunur.
* [cite_start]Veri tabanından çekilen belgenin şifresi çözülmeden arayüzdeki düzenleme alanları (`EditText`'ler, satır/sütun ekleme butonları) tamamen **kilitli (disabled)** ve salt okunur modda kalır.
* [cite_start]Kullanıcı doğru şifreyi girip "Kilidi Aç" butonuna bastığında arayüz aktifleşir ve güncelleme yetkisi verilir.

### 4. Çevrimdışı Çalışma Desteği (Offline Persistence)
* [cite_start]Firebase Firestore altyapısı `PersistenceEnabled = true` olarak yapılandırılmıştır. [cite_start]Bu sayede daha önce taranan dinamik QR verileri cihaz hafızasına önbelleklenir; internet bağlantısı tamamen kopsa bile kullanıcı karekodu tarattığında güncel verileri internetsiz ortamda da görebilir.

<img width="1344" height="2992" alt="Screenshot_20260711_150206" src="https://github.com/user-attachments/assets/05ee3d18-652b-420d-a96e-9cbdc5daabf7" />
<img width="1344" height="2992" alt="Screenshot_20260711_150312" src="https://github.com/user-attachments/assets/d9781a6e-1c77-472c-9ffc-be71dc743e6b" />
<img width="1344" height="2992" alt="Screenshot_20260711_150849" src="https://github.com/user-attachments/assets/3b6d8221-bea0-441d-a271-90ca340061eb" />


---

## 🛠️ Teknik Mimari ve Kullanılan Teknolojiler

* **Minimum SDK:** 24 (Android 7.0 Nougat ve üzeri)
* **Dil & Ekosistem:** Kotlin, Jetpack (Saf Android)
* [cite_start]**Veri Tabanı:** Firebase Firestore (NoSQL Bulut Veri Tabanı) 
* [cite_start]**QR Kütüphaneleri:** * `com.github.budiyev:code-scanner` (Kamera tarama motoru) 
  * [cite_start]`com.google.zxing:core` (QR Kod oluşturma motoru) 

---

## 📂 Detaylı Kod Yapısı ve Sınıf Görevleri

### 📄 `MainActivity.kt`
* [cite_start]Uygulamanın giriş kapısıdır. [cite_start]`CodeScanner` kütüphanesini yönetir, kamera izinlerini kontrol eder. [cite_start]Taranan verinin standart bir metin mi yoksa buluttan çekilecek dinamik bir kod mu olduğunu ayırt eden regex/filtreleme mantığı burada çalışır.

### 📄 `DynamicQrActivity.kt`
* [cite_start]Uygulamanın en karmaşık ve detaylı sınıfıdır. 
* [cite_start]**Veri Yönetimi:** Firestore'daki `dynamic_qrs` koleksiyonuna bağlanarak ilgili dökümanı gerçek zamanlı dinler.
* [cite_start]**Şifre Algoritması:** Kullanıcının girdiği şifreyi veri tabanındaki `password` alanı ile eşleştirerek arayüzün `isEnabled` durumunu yönetir.
* [cite_start]**Dinamik Arayüz:** Tablo modunda, XML tarafındaki `TableLayout` içerisine Kotlin koduyla dinamik olarak `TableRow` ve `EditText` elemanları enjekte eder. [cite_start]Verileri virgül (`,`) ve noktalı virgül (`;`) işaretlerine göre parçalayarak tabloya doldurur veya tam tersi şekilde paketleyip Firestore'a günceller.

### 📄 `QrGeneratorActivity.kt`
* [cite_start]Kullanıcının girdiği herhangi bir metni veya bağlantıyı anlık olarak `ZXing` kütüphanesi aracılığıyla işler, bir `Bitmap` grafiğine dönüştürür ve ekrandaki `ImageView` üzerinde statik QR kod olarak gösterir.

### 📄 `InfoActivity.kt`
* [cite_start]Uygulamanın versiyonlama, geliştirici bilgileri ve kurumsal detaylarını şık bir arayüzle kullanıcıya sunan bilgi ekranıdır.

---

## ⚙️ Kurulum ve Yapılandırma

1. Projeyi klonlayın:
   ```bash
   git clone [https://github.com/MertMusaYazar/QRMixApp.git](https://github.com/MertMusaYazar/QRMixApp.git)![Uploading Screenshot_20260711_150206.png…]()
<img width="1344" height="2992" alt="Screenshot_20260711_150849" src="https://github.com/user-attachments/assets/837b55b0-8330-4fd5-a7bf-05626f541fb5" />
<img width="1344" height="2992" alt="Screenshot_20260711_150312" src="https://github.com/user-attachments/assets/7e77a435-601b-4790-9506-fe94f71a1089" />
