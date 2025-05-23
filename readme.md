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
  
  8. **Performans odaklı sistem yapılandırması** : İkiz, hangi sınıfın hangi alanlarının verisinin kaydedileceği, veritabanından getirilen verinin hangi tipe çevrileceğiyle ilgili analizleri sürekli yapmak yerine bir sefer yapar ve kaydeder. Bu, sistem yapılandırma dosyası olarak dışarı aktarılabilir, uygulama yeniden başlatıldığında ilgili veri okunarak ayarlar yeniden içe aktarılabilir; belki daha da iyisi İkiz bu sistem yapılandırmasını veritabanını okuyarak çıkartabilir; bu ayarlar tablo kısıtları gibi sâir önverileri de kapsamaktadır (Saklı yordamları henüz desteklemiyoruz, tek kişilik ekibimiz var, o kadar hızlı değiliz!).
  
  9. **Dizi ve Koleksiyon Verilerinin Veritabanında Saklanması** : İkiz, uygulama içerisinde tanımlanan temel (`int`, `double`...) veyâ sarmalayıcı sınıfların (`Integer`, `Double`...) dizilerini, çok boyutlu dizilerini, `List` ve `Map` sınıfındaki verileri ve `Collection` arayüzünü uygulayan diğer sınıfların verilerini veritabanında tek sütunda tutmayı destekler; bunun için verileri JSON metnine çevirir ve hedef veritabanında JSON metninin saklanması için en uygun olan veri tipinde bir sütun oluşturarak verileri veritabanına kaydeder; ayrıca dilerseniz bu tipteki verilerin veritabanına hiç kaydedilmemesini de destekler.
  
  10. **Bağımsız tasarım** : İkiz'in, veritabanı arayüz bağlantı sağlayıcısı (connector) dışında bir bağımlılığı yoktur; yardımcı olarak kullanılan `ReflectorRuntime` ve `jsoner` kitâplıkları da sıfırdan yazılarak uygulamaya dâhil edilmiştir.
  
  11. **Geliştirilebilir tasarım** : 'Bundan iyisi Özgür Suriye'deki Şam'da kayısı' demiyoruz ve geliştirmeye devâm ediyoruz, vaktimin bir kısmını bu projeye ayırdığım için yeni sürüm yakın olmayabilir.

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

- Bu sürümde gelişmiş sorgulama desteği yoktur.

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
  
  `mkdir -p ikiz && mkdir -p ikiz/libs && echo Manifest-Version: 1.0 > ikiz/MANIFEST.MF && echo Class-Path: libs/rwservice.jar libs/ReflectorRuntime.jar libs/jsoner.jar >> ikiz/MANIFEST.MF && cp libs/*.jar ikiz/libs && javac -encoding UTF-8 -cp libs/*:src/ikiz -parameters -d ikiz src/ikiz/*.java src/ikiz/Services/*.java && cd ikiz && jar mcf MANIFEST.MF ikiz.jar ikiz/*.class ikiz/Services/*.class`

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

- İkiz, alan verilerini alırken öncelikle `java.lang.reflect` arayüzünü kullanarak alanı temsîl eden `Field` nesnesi üzerinden veriye erişmeye çalışır. Eğer alanınızın erişim belirteci buna izin vermiyorsa, İkiz alan verisini alabilmek için kendi yapılandırmasında belirtilen kodlama biçimine göre (`ReflectorRuntime.Reflector.CODING_STYLE`) 'getter' metodunu arar ve onu çalıştırarak alan verisini alır; eğer böyle bir metot yoksa, alan verisi alınamaz.

- İkiz'in 'getter' ve 'setter' metotlarını ararken kullandığı kodlama biçimi varsayılan olarak camelCase kodlama biçimidir. Kodlama biçimini değiştirebilirsiniz; daha fazla bilgi için İkiz'in arkaplan işlemleri için yazdığım ReflectorRuntime kitâplığına bakınız.
  
  ```java
  ikiz.getConfs().
      setCodingStyleForGetterSetter(Reflector.CODING_STYLE.SNAKE_CASE);
  // Yukarıdaki ayara göre İkiz, 'id' alanı için 'setter' metodunu
  // 'set_id' ismiyle, 'getter' metodunu 'get_id' ismiyle arar
  ```

- Sınıf alanlarınızın hangi erişim belirteciyle belirtilenlerin alınacağını İkiz yapılandırmasından değiştirebilirsiniz; varsayılan olarak tüm erişim belirteciyle tanımlanan alanlar alınır. Erişim belirtecine göre alım yapılandırmasını değiştirebilirsiniz:
  
  ```java
  ikiz.getConfs().setAttributesPolicyOneByOne(true, false, true, true);
  // Fonksiyon imzası: public void setAttributesPolicyOneByOne(
  // boolean takePublicFields, boolean takePrivateFields,
  // boolean takeDefaultFields, boolean takeProtectedFields);
  ```

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

- Eğer tablonuzda bir birincil anahtar varsa İkiz, verilerinizin uygulama içerisinde münferid (tekil) olmasını sağlar. Bunun için önbellekleme modunu etkinleştirmelisiniz.

- Önbellekleme modunu şu şekilde etkinleştirebilirsiniz:
  
  ```java
  ikiz.getConfs().setBufferMode(true);
  ```

- Eğer önbellekleme modu etkin ise, İkiz verilerinizi her seferinde veritabanından getirmek yerine önbellekten getirir.

- Bu durumda, verilerinizin veritabanından taze çekilmesini isterseniz veritabanı - tablo eşleşmesi için şu metotları kullanabilirsiniz:
  
  ```java
  ikiz.syncDataByPullFromDB(Customer.class);// Customer verisi tazelenir
  ikiz.syncDataByPullFromDB();// Tüm veriler vt'den çekilirek tazelenir
  ```

- Tablonun sadece belli sütunlarının değerlerini çekebilirsiniz:
  
  ```java
  List<Object> listOf = ikiz.getColumnValues(Customer.class, "name");
  ```

- Sütun verilerini yukarıdaki gibi çekerseniz, veriler dönüştürülmez ve veritabanı JDBC bağlayıcınız tarafından hangi tipte döndürülüyorsa, o tipte döndürülür; `null` veriler de getirilir.

- Dilerseniz, verilerinizi almak istediğiniz hedef tipi belirtebilirsiniz:
  
  ```java
  List<String> listOfNames = ikiz.getColumnValues(
              Customer.class, "name", String.class);
  ```

- Bu şekilde yaptığınızda `null` veriler getirilmez.

- Birden fazla sütun verisini ham olarak (İkiz'in JSON dönüştürme, basit casting, gelişmiş casting yapıları kullanılmaz) çekmek isterseniz şu fonksiyonları kullanın:
  
  ```java
  List<String> cols = new ArrayList<String>();
  cols.add("name");
  cols.add("surname");
  List<Map<String, Object> values = ikiz.getValuesFromTable(
                                     Customer.class, cols);
  // Listenin her elemanı bir satırın anahtar - değer ikililerini tutar
  // Bu örnekte sadece 'name' ve 'surname' alan değerleri tutulur.
  ```

#### 2) Veri Ekleme

- Oluşturduğunuz bir nesneyi İkiz ile veritabanına eklemek basittir:
  
  ```java
  Customer csm = new Customer();
  boolean result = ikiz.addRowToDB(csm);
  System.out.println("Veri ekleme başarı durumu : " + result);
  ```

- Eğer `Customer` sınıfı İkiz'e kayıtlı ise, veriniz eklenir.

- Gönderilen verinin alan değerleri, tablo oluşturulurken kullanılan `IkizIdare` yapılandırmasına göre alınır.

- Hedef sınıfın alan değerleri alınırken aranması gereken 'getter' ve 'setter' metot isminin hangi kodlama biçimine göre yapılacağı İkiz yapılandırması üzerinden değiştirilebilir.

#### 3) Veri ve Tablo Silme

- İkiz ile bir veriyi silmek için silmek istediğiniz nesneyi hedef metoda vermeniz kâfî:
  
  ```java
  boolean result = ikiz.deleteRowFromDB(csm);
  System.out.println("Veri silme işlemi sonucu: " + result);
  ```

- Burada dikkat edilmesi gereken nokta, tablonun İkiz'in veriyi, diğer verilerden ayırt etmesini sağlayan bir yapıyı barındırması gerektiğidir. Buna göre bir satırı diğerlerinden ayıran birincil anahtar veyâ 'NOT NULL + UNIQUE' kısıtlarını taşıyan herhangi bir sütun olmayan tablodan müşahhas bir veri silemezsiniz.

- Bilhassa verinin ayırt edilmesi meselesi sebebiyle İkiz'e kaydedilmeyen tablolardan veri silme işlemini bu metotla gerçekleştiremezsiniz.

- Bir tabloyu veritabanından silmek için şu metodunu kullanın:
  
  ```java
  ikiz.deleteTable(Customer.class);// Tablo ikiz'den ve vt'den silinir
  ```

#### 4) Veri Tazeleme (Güncelleme, 'UPDATE')

- Veri tazelemek için mevcut nesneyi vermeniz kâfî:
  
  ```java
  result = ikiz.updateRowInDB(csm);
  System.out.println("Veri tazeleme sonucu: " + result);
  ```

- Veriniz büyük olabilir ve ağ trafiğini arttırmamak için verilerinizin yalnızca bâzı sütunlarını değiştirmek istiyor olabilirsiniz. Bu durumda, veri değişikliği yapmak istediğiniz alanların listesini verebilirsiniz:
  
  ```java
  // Sadece belirli alanları tazeleme:
  fieldsToUpd.add("info");
  fieldsToUpd.add("ipAddress");
  result = ikiz.updateRowInDB(csm, fieldsToUpd);
  System.out.println("Veri tazeleme sonucu: " + result);
  ```

## İkiz Yapılandırması, Yapılandırma Yedekleme ve Yapılandırma Geri Yükleme

- İkiz'in kendi yapılandırması ile tablo yapılandırması farklı şeylerdir. İkiz'in yapılandırması `ikiz.Confs` sınıfıyla ifâde edilir; tablo yapılandırması ise `ikiz.TableConfiguration` sınıfıyla temsil edilir.

- İkiz yapılandırması az sayıda ayardan oluşmaktadır. Başlangıçta tasarlanan bâzı yapılandırmalar bu sürümde yer almamaktadır. İkiz yapılandırma ayarlarına erişmek ve bunları değiştirmek için `IkizIdare` nesnesi üzerinden `getConfs()` metodunu kullanabilirsiniz.

- Kullanılabilecek olan yapılandırma ayarları şunlardır:
  
  - **attributesPolicy** : Bu yapılandırma İkiz'in sınıf alanlarını ele alırken hangi erişim belirteciyle tanımlanmış alanları alacağını belirtmek için kullanılır. Varsayılan olarak tüm sınıf alanları ele alınır.
  
  - **policyForListArrayMapFields** : İkiz'in dizi, liste, koleksiyon ve harita (`Map`) tipindeki alanları nasıl ele alacağını ifâde eden bu yapılandırma için `TAKE_AS_JSON` ve `DONT_TAKE` seçenekleri vardır.
  
  - **codingStyleForGetterSetter** : Bu yapılandırma, İkiz'in sınıflardaki alan verilerine erişirken doğrudan erişme yetkisinin olmadığı alanlara erişmek için arayacağı 'getter' ve 'setter' metodlarını hangi isimle arayacağını belirten bir kodlama biçimi ayarıdır.
  
  - **bufferMode** : İkiz'in verilerin önbelleklenmesini istiyorsanız bu ayar için `true` değeri vermelisiniz. İkiz'in birincil anahtarı olan tablolardaki verileri uygulamadaki karşılığı olan nesneleri münferid (tekil) olarak tutması için bu seçenek açık olmalıdır.

- İkiz yapılandırmasını şu şekilde kaydedebilirsiniz. Birincisi `ikiz` yapılandırması + tablo yapılandırmalarını kaydeder, ikincisi sadece `ikiz` yapılandırmasını kaydeder:
  
  ```java
  boolean res=ikiz.saveAllConfiguration("C:\\NBProjects\\Extraction");
  res = ikiz.saveJustIkizConfiguration("C:\\NBProjects\\Extraction");
  ```

- Sadece İkiz yapılandırması kaydedildiyse dosyanız şu verilerin tek satırda yazılmış hâlidir; 'alwaysContinue' ayarı bu sürümde kullanılmıyor:
  
  ```json
  {
      "IkizConfigurations": {
          "attributesPolicy": {
              "private": true,
              "default": true,
              "public": true,
              "protected": true
          },
          "policyForListArrayMapFields": "TAKE_AS_JSON",
          "codingStyleForGetterSetter": "CAMEL_CASE",
          "alwaysContinue": true,
          "bufferMode": true
      }
  }
  ```

- Sistemi yeniden başlattığınızda, mevcut yapılandırmalarınızın geri yüklenmesi için iki seçenek kullanılmaktadır.
  
  1. VT şeması analizi ile: Sistemi, bağlantısını verdiğiniz veritabanı şemasının analizini yaptırarak geri yükleyebilirsiniz. Bu durumda tablo yapılandırmalarını geri kazanmış olursunuz; fakat İkiz yapılandırmalarını yüklememiş olursunuz; çünkü İkiz yapılandırması veritabanında saklanmaz. İkiz yapılandırması sadece birkaç ayar olduğu için varsayılan ayar dışındaki ayarlarınızı İkiz üzerinden seçtiğiniz bir fonksiyon yazıp, sistemi her başlattığınızda bu fonksiyonu çalıştırabilir veyâ sadece İkiz yapılandırmasını dosyadan yükleyebilirsiniz:
     
     ```java
     boolean result = ikiz.loadSystemByAnalyzingDB();
     //System.out.println("VT analiziyle sistem yüklemesi: " + result);
     result = ikiz.
        importIkizConfigurationsFromFile("C:\\NBProjects\\Extraction");
     System.out.println("İkiz yapılandırması içe aktarımı: " + result);
     
     // Dosya adresi yerine doğrudan JSON metni vermek isterseniz
     // şu metodu kullanın : importIkizConfigurations(jsonText)
     ```
  
  2. Kaydettiğiniz yapılandırma ayarları ile : Kaydettiğiniz yapılandırma ayarları üzerinden İkiz sistemini yükleyebilirsiniz. Eğer bu yapılandırma verisi içerisinde yüklenemeyen bir tablo yapılandırması olursa hatâ alırsınız. Bu metot hem İkiz yapılandırmasını, hem de sistem yapılandırmasını içe aktarır:
     
     ```java
     String path = "C:\\NBProjects\\Extraction";
     boolean result = ikiz.loadSystemFromAllConfigurationsFile(path);
     System.out.println("Dosyadan sistem yüklemesi : " + result);
     
     // Dosya adresi yerine doğrudan JSON metni vermek isterseniz
     // şu metodu kullanın : loadSystemFromAllConfigurations(jsonText)
     ```

## GÜVENLİK

- Tablo oluşturulduktan sonra tablo yapılandırması uygulama içerisinden değiştirilemektedir; fakat veritabanında ilgili yapılandırma değişikliğini yaptıktan sonra sistemi veritabanı analiziyle başlatarak veyâ yapılandırmayı dosyadan yüklüyorsanız ilgili yapılandırmayı dosya üzerinde yaparak yeni tablo yapılandırma ayarlarını sisteme geçirebilirsiniz.

- SSL ile bağlanma için ayar belirtme desteği henüz yoktur.

## DİĞER

- Hedef şemanızdaki bİr tabloyu `ikiz`'den çıkartabilirsiniz:
  
  ```java
  ikiz.discardTableFromIkiz(Customer.class);
  ```

- Eğer sistemdeki bir tabloyu İkiz'den çıkarırsanız, İkiz, artık bu tablo için yalnızca veri çekme işlemi yapabilecektir.

- ..
