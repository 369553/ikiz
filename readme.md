# ikiz Kullanıcı El Kitâbı (ikiz v1.0-beta)

## Genel Bilgiler

- `ikiz` bir ORM (Object Relation Mapping - Nesne İlişki Haritalandırması) çözümüdür.

- Bir ORM, uygulama içerisindeki nesneleri veritabanındaki satırlarla eşleştiren yardımcı bir kitâplıktır.

- İkiz MySQL, MsSQL veritabanlarını desteklemektedir; PostgreSQL ve SQLite veritabanlarının desteklenmesi bu sürüm için ertelenmiştir.

- İkiz'in desteklediği ORM çözümleri:
  
  1. **Uygulama içerisindeki bir sınıftan tablo üretimi** : İkiz, barındırdığı yöntemlerle ister kendi yapılandırmanızı yaparak, isterseniz de varsayılan yapılandırmayla bir sınıftan otomatik tablo üretebilir.
  
  2. **Veritabanından kolay veri çekimi** : İkiz, sadece ilgili sınıfı verdiğinizde veritabanından o sınıfın temsil ettiği verileri kolayca çekebilir.
  
  3. **Verilerin zerk edilmesiyle otomatik nesne üretimi** : İkiz, veritabanından çektiğiniz verilerin nesnelere zerk edilmesiyle otomatik olarak uygulama nesnelerini elde etmenize imkân sağlar.
  
  4. **Önbellekleme** : İkiz, kullanıcının tercihine bağlı olarak verileri önbellekleyebilir; bu, sisteme -uygulanabilir olduğu durumlarda- kullanıcının isteklerine veritabanına gitmeden cevâp verme kâbiliyyeti kazandırmaktadır.
  
  5. **Münferid (tekil) nesne sağlama** : İkiz, kullanıcının önbellekleme modunu açması şartıyla birincil anahtar sağlanan tablolardan çektiği verileri uygulama içerisinde münferid olarak sağlar; bunun için ilgili veriler uygulama içerisinde `IkizMunferid` sınıfıyla desteklenen indekslemeyle tutulur. Kullanıcı ilgili nesneye birincil anahtarla erişebilir.
  
  6. **Veritabanı tablo yapılandırması** : İkiz, kullanıcıya veritabanındaki tablolarına birincil anahtar, münferid (`UNIQUE`) anahtar, varsayılan değer kısıtı, `NULL` veriyi engelleme kısıtı (`NOT NULL`) gibi kısıtlar eklemesi için kolay arayüz sağlar.
  
  7. **CRUD işlemleri** : İkiz CRUD (Create-Update-Delete) işlemleri için yöntemler sağlar. İlgili nesnelerin sadece seçilen özelliklerinin veritabanına kaydedilmesi veyâ tüm alanların yeniden tazelenmesi gibi seçenekler sağlar.
  
  8. **Gelişmiş sorgulama** : Onu daha yapmadık!.. Şu an için bir sütuna göre veyâ birincil anahtara göre veri sorgulaması yapılabiliyor.
  
  9. **Performans odaklı sistem yapılandırması** : İkiz, hangi sınıfın hangi alanlarının verisinin kaydedileceği, veritabanından getirilen verinin hangi tipe çevrileceğiyle ilgili analizleri sürekli yapmak yerine bir sefer yapar ve kaydeder. Bu, sistem yapılandırma dosyası olarak dışarı aktarılabilir, uygulama yeniden başlatıldığında ilgili veri okunarak ayarlar yeniden içe aktarılabilir; belki daha da iyisi İkiz bu sistem yapılandırmasını veritabanını okuyarak çıkartabilir; bu ayarlar tablo kısıtları gibi sâir önverileri de kapsamaktadır (Saklı yordamları henüz desteklemiyoruz, tek kişilik ekibimiz var, o kadar hızlı değiliz!).
  
  10. **Dizi ve Koleksiyon Verilerinin Veritabanında Saklanması** : İkiz, uygulama içerisinde tanımlanan temel (`int`, `double`...) veyâ sarmalayıcı sınıfların (`Integer`, `Double`...) dizilerini, çok boyutlu dizilerini, `List` ve `Map` sınıfındaki verileri ve `Collection` arayüzünü uygulayan diğer sınıfların verilerini veritabanında tek sütunda tutmayı destekler; bunun için verileri JSON metnine çevirir ve hedef veritabanında JSON metninin saklanması için en uygun olan veri tipinde bir sütun oluşturarak verileri veritabanına kaydeder; ayrıca dilerseniz bu tipteki verilerin veritabanına hiç kaydedilmemesini de destekler.
  
  11. **Bağımsız tasarım** : İkiz'in, veritabanı arayüz bağlantı sağlayıcısı (connector) dışında bir bağımlılığı yoktur; yardımcı olarak kullanılan `ReflectorRuntime` ve `jsoner` kitâplıklarını da ben yazdım ve uygulamaya dâhil ettim.
  
  12. **Geliştirilebilir tasarım** : 'Bundan iyisi Özgür Suriye'deki Şam'da kayısı' demiyoruz ve geliştirmeye devâm ediyoruz, vaktimin bir kısmını bu projeye ayırdığım için yeni sürüm yakın olmayabilir.

- ..

#### Sistem Kısıtları - Eksiklikleri

- 'Bire bir isim kısıtı' sebebiyle farklı paketlerdeki sınıflar aynı veritabanında kullanılamaz; bir tablonun veritabanında olup, olmadığını kontrol etmek için `IkizIdare` nesnesi üzerinden `checkIsTableInDB(String tableName)` yöntemini kullanabilirsiniz.

- İkiz sistemi kullanıcının tanımladığı bir sınıftaki alanların hangilerinin alınacağı hususunda kullanıcıya birkaç yapılandırma seçse de, alınan alanların dışlanacağı bir yapıyı bu sürümde sunmamaktadır.

- İkiz sistemi kullanıcının tanımladığı sınıftaki alanları alırken tip kontrolü yapar. **Kullanıcı tanımlı veri tipi bir özellik ('field') bu sürümde alınmamaktadır**.

- İkiz, bir takım oyuncusu olan `IkizMunferid` ile veritabanında çekilen verilerin uygulama içerisinde münferid (tekil, 'unique') olmasını destekler; fakat bu destek için tabloda satırları birbirinden ayıran bir birincil anahtar olması zorunludur.

- İkiz sistemi, müşahhas bir verinin silinmesi veyâ tazelenmesi için tabloda bir birincil anahtar veyâ '**NOT NULL + UNIQUE**' kısıtını taşıyan bir sütun olmasını şart koşar.

- Müşahhas bir veri üzerinde silme işlemi gerçekleştirmek için 'NOT NULL + UNIQUE' kısıtı kâfî iken, verinin uygulama içerisinde münferid olarak bulunması için birincil anahtar olması şartı vardır (ilk sürümde böyle zayıflıklar olabiliyor).

- Bu sürümde, metîn tipindeki bir sütun için karakter seti belirleme ve varsayılan karakter seti belirleme desteği yoktur.

- İkiz yapılandırması, veritabanında saklanmadığından İkiz'i veritabanını analiz ederek çalıştırdığınızda tablo yapılandırması aynen kazanılacaktır; fakat İkiz'in özellikleri alma, önbellekleme modunun açık olup, olmaması gibi yapılandırmalar varsayılana döndürülecektir. Bu sebeple, İkiz yapılandırmasını bir dosyaya alabilir veyâ varsayılan yapılandırma üzerinde yapacağınız değişiklikleri bir kod bloğu olarak uygulama içerisinde saklayabilirsiniz (zâten birkaç tâne ayar var).

- İkiz sistemi hatâ ve kayıt('log') ların tutulması için bu sürümde destek sunmamaktadır.

- Verilerin bir sayaca bağlı olarak arkaplanda otomatik tazelenmesi desteği henüz yoktur.

- Sistem büyük - küçük harf duyarsız veritabanıyla çalışmaktadır; veritabanı yapılandırması büyük - küçük duyarlı olacak şekilde ayarlandıysa, bir çakışma olmadığı sürece sistem çalışabilir; fakat olası hatâ noktaları değerlendirilmemiştir.

- Veritabanı sütun isimleri sınıf özellik ismiyle aynı olur, farklı isim desteği yoktur.

- Tabloya sadece bir sütun üzerinde çalışabilen `UNIQUE` kısıtı eklenir; iki sütun üzerine eklenen `UNIQUE` indeksleri bu sürümde çalışmamaktadır.

## Derleme ve Çalıştırma

- İkiz'i bir JAR ve yardımcı kitâplıklarıyla berâber derleyip, uygulamanıza koyabilirsiniz.

- `dist` dizini altında JAR dosyası ve gereken kütüphâneler var, oradaki dosyaların projeye kopyalanması kâfîdir.

- Eğer elle derleme yapmak istiyorsanız şu adımları tâkip edebilirsiniz:

- Bunun için öncelikle `ikiz`'i klonlayın:
  
  ```shell
  git clone https://github.com/369553/ikiz.git
  ```

- 'ikiz' dizini içerisine girin ve o dizinde komut satırını / uç birimi açın.

- Ardından, Windows kullanıyorsanız şu komutu çalıştırın:
  
  `mkdir ikiz & mkdir ikiz\libs & echo Manifest-Version: 1.0 > ikiz/MANIFEST.MF & echo Class-Path: libs/rwservice.jar libs/ReflectorRuntime.jar libs/jsoner.jar >> ikiz/MANIFEST.MF & copy .\libs\*.jar .\ikiz\libs & javac -encoding UTF-8 -cp libs\*;src\ikiz -parameters -d ikiz src\ikiz\*.java src\ikiz\Services\*.java & cd ikiz & jar mcf MANIFEST.MF ikiz.jar ikiz/*.class ikiz/Services/*.class`

- Linux kullanıyorsanız, şu komutu çalıştırın:
  
  `&& mkdir -p ikiz && mkdir -p ikiz/libs && echo Manifest-Version: 1.0 > ikiz/MANIFEST.MF && echo Class-Path: libs/rwservice.jar libs/ReflectorRuntime.jar libs/jsoner.jar >> ikiz/MANIFEST.MF && cp libs/*.jar ikiz/libs && javac -encoding UTF-8 -cp libs/*:src/ikiz -parameters -d ikiz src/ikiz/*.java src/ikiz/Services/*.java && cd ikiz && jar mcf MANIFEST.MF ikiz.jar ikiz/*.class ikiz/Services/*.class`

- Ardından bulunduğunuz dizin altındaki '*ikiz*' dizini altındaki '*ikiz.jar*' dosyasını ve 'libs' dizinini kopyalayın ve `ikiz`'i proje dosya yolunuza ekleyin.

## Sistemi Başlatma ve Çalıştırma

- İkiz sistemini başlatmak için bir bağlantıya ihtiyacınız var. İkiz'de bağlantı `Cvity` isimli bir sınıfla temsil ediliyor. Bu sınıfla bağlantı oluşturmadan evvel veritabanına bağlanmayı sağlayan arayüz dosyanızı (connector) proje dosya yolunuza ('classpath') eklediğinizden emîn olun.

- `Cvity` üzerinden bağlantı oluşturup, bu bağlantıyla `Cvity` nesnesi oluşturun:
  
  ```java
  Connection conToDB = Cvity.connectDB("root",
          "passwordOfRoot", "localhost", 3306,
          "ikizTest", Cvity.DBType.MYSQL);// Son parametre vt tipi
  Cvity connectivity = new Cvity(conToDB, "root", "passwordOfRoot",
              "ikizTest");
  ```

- Oluşturduğunuz `Cvity` nesnesiyle `ikiz`'i başlatmak için `IkizIdare` sınıfının `startIkizIdare()` statik fonksiyonunu kullanın:
  
  ```java
  boolean isStarted = IkizIdare.startIkizIdare(connectivity);
  // Bağlantınız test edilir ve İkiz'in başlatılma durumu döndürülür.
  System.out.println("İkiz sisteminin başlatılma durumu: " + isStarted);
  // Oluşturulan ikiz idâreci sınıfına statik yöntemle erişin:
  IkizIdare ikiz = IkizIdare.getIkizIdare();
  ```

- Eğer sistemi yeni başlatıyorsanız sistemi başlatmak bu kadar.

#### Sistemi Veritabanı Analiziyle Yükleme

- Eğer tablolarınız hâli hâzırda veritabanında kayıtlıysa ve bu veritabanı nesneleriniz için uygulama sınıflarınız mevcutsa sistemi veritabanı analiziyle yükleyebilirsiniz:
  
  ```java
  boolean result = ikiz.loadSystemByAnalyzingDB();
  System.out.println("VT analiziyle sistemin yüklenmesi: " + result);
  ```

- İkiz, bağlantısını verdiğiniz veritabanındaki tabloların bilgilerini alır; bu bilgilerden tablo yapılandırma sınıflarını (`TableConfiguration`) oluşturur ve uygulama yolunuzdaki sınıfları tarayarak tablonun eşleştiği sınıfı bulur; bulamadığı sınıfları es geçer.

- İkiz, VT sınıflarınızı ararken uygulama dizinizi tarar ve tablo ismiyle aynı isimde bir sınıf arar. Buradaki isim, büyük - küçük harf duyarsızdır. Eğer uygulama dizininizde farklı modüller altında hedef tabloyla aynı isimde birden fazla sınıf varsa, İkiz sınıfların tabloyla uyumunu test ederek doğru sınıfı tespit eder.

- İkiz'i bir yapılandırma yedeğinden geri yüklemeniz de mümkündür; bu konuya ileride değinilecektir inşâAllâh..

## Tablo Yapılandırması ve Tablo Oluşturma

- Tablo oluşturmak için `IkizIdare` nesnenizin `produceTable()` metotlarını kullanın:
  
  ```java
  // Misal, Customer sınıfınızın olduğunu varsayalım..
  boolean isTableCreated = ikiz.produceTable(Customer.class);
  System.out.println("Tablo oluşturulma durumu: " + isTableCreated);
  ```

- **Eğer bağlandığınız veritabanı şemasında aynı isimde bir tablo varsa, işlem başarısızlıkla sonuçlanır.**

- İkiz tablonuz için yapılandırma desteği sunar. Tablonuzu yapılandırmak için kullanımı konforlu olan `TableConfiguration` sınıfını kullanabilirsiniz:
  
  ```java
  TableConfiguration tblConfs = new TableConfiguration(Customer.class);
  ```

- Bu kod ile tablonuz için bir yapılandırma oluşturulmuş oldu. `TableConfiguration` sınıfı varsayılan olarak verdiğiniz sınıfın **alınabilecek** tüm alanlarını alır; kullanıcı tanımlı sınıf tipindeki alanların bu sürümde alınmadığını unutmayınız.

- Eğer sınıfınızda olan bir alanın veritabanında olmasını istemiyorsanız tablo yapılandırması nesnesi üzerinden bunu belirtebilirsiniz:
  
  ```java
  tblConfs.discardField("ilgiliAlanınIsmi");// Bir sütunu çıkarma
  ```

- Tablo yapılandırması üzerinden tablonuzla ilgili şu işlemleri yapabilirsiniz:
  
  - Birincil anahtar ekleyebilirsiniz
  
  - Birincil anahtarı otomatik artan yapabilirsiniz
  
  - Metîn tipindeki bir alanınızın azamî karakter sayısını belirleyebilirsiniz.
  
  - Tüm metîn alanları için **varsayılan** azamî karakter sınırını atayabilirsiniz; bu sınır özel olarak belirttiğiniz metîn alanlarını etkilemez.
  
  - Bir sütun için varsayılan değer ekleyebilirsiniz.
  
  - Sütun için boş olmama (`NOT NULL`) kısıtı ekleyebilirsiniz.
  
  - Bir sütuna münferid (`UNIQUE`) değer kısıtı ekleyebilirsiniz.

- Yukarıda sıralanan işlevler sırasıyla şu kodlarla yapılır:
  
  ```java
  tblConfs.setPrimaryKey("id");// Birincil anahtar ata
  tblConfs.setPrimaryKeyAutoIncremented();//Bu anh.'ı otomatik artan yap
  tblConfs.setLengthOfString("info", 2000);//info için maks karakter ata
  tblConfs.setDefaultLengthOfString(1000);// Tüm metînler için max sınır
  tblConfs.addDefaultValue("yearAtHere", 0);// Varsayılan değer atama
  tblConfs.addNotNullConstraint("name");// 'NOT NULL' kısıtı ekleme
  tblConfs.addUniqueConstraint("ipAddress");// 'UNIQUE' kısıtı ekleme
  ```

- Tablo oluşturulmadığı müddetçe bu yapılandırmalarla oynayabilirsiniz; tablo oluşturulduktan sonra bu yapılandırmaları değiştiremezsiniz.

- Oluşturduğunuz tablo yapılandırmasını kullanmak için `produceTable()` metoduna parametre olarak verin:
  
  ```java
  boolean isTableCreated = ikiz.produceTable(Customer.class, tblConfs);
  ```

> ***NOT :*** İkiz, yapılandırmalarınızın veritabanı sistemiyle tamâmen uyumluluğunu denetlemez; bâzı kontroller yapsa da yapılandırmalarınız veritabanıyla uyumluluğu sizin kontrolünüzdedir.

## Sınıf Alanlarınız Nasıl Ele Alınıyor

- İkiz, bu sürümde sınıftaki kullanıcı tanımlı alanlarınızı ele almaz.

- Sayı, metîn, târih, târih - saat, saat, 'enum' gibi temel veri tipinde olan alanlarınızı destekler.

- Dizi, matris, `List<T>`, `Map<T, V>` gibi koleksiyonlarınız için iki seçenek sunulmaktadır. Birincisi bu alanların alınmaması, ikincisi ise bu alanların JSON verisi olarak veritabanında saklanmasıdır.

- Veritabanında JSON verisi olarak saklanan alanlarınız uygulamaya hedef veri tipine çevrilerek getirilir.

- Sınıf alanlarınızın hangi erişim belirteciyle belirtilenlerin alınacağını İkiz yapılandırmasından değiştirebilirsiniz; varsayılan olarak tüm erişim belirteciyle tanımlanan alanlar alınır.

## Diğer Temel Veritabanı İşlemleri

#### 1) Veri Çekme

- Tablonuz İkiz'e kaydedilmiş olsun veyâ olmasın, İkiz ile tablo verilerinizi veritabanından çekebilirsiniz:
  
  ```java
  // Tüm veriyi çekme:
  List<Customer> allData = ikiz.getData(Customer.class);
  
  // Birincil anahtar ile veri çekme:
  Customer csm = ikiz.getDataById(Customer.class, 2);
  
  // Bir sütun için WHERE şartı belirtme:
  List<Customer> listOfNewOnes = 
     ikiz.getDataWithOneWhereCondition(Customer.class, "yearAtHere", 0);
  ```

- Eğer tablonuzda bir birincil anahtar varsa, İkiz verilerinizin uygulama içerisinde münferid (tekil) olmasını sağlar. Bunun için önbellekleme modunu etkinleştirmelisiniz.

- Eğer önbellekleme modu etkin ise, İkiz verilerinizi her seferinde veritabanından getirmek yerine önbellekten getirir.

- .

- .

#### 2) Veri Ekleme

- Oluşturduğunuz bir nesneyi İkiz ile veritabanına eklemek basittir:
  
  ```java
  ikiz.addRowToDB
  ```

- .

- .

- .

- .

## GÜVENLİK

- Tablo oluşturulduktan sonra tablo yapılandırması değiştirilemektedir.

- SSL ile bağlanma için ayar belirtimi henüz yoktur.

- .
