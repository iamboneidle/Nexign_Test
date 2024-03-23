# Nexign test task
## Дано
Все звонки, совершенные абонентом сотового оператора, фиксируются в CDR файлы, которые собираются на коммутаторах. Когда абонент находится в роуминге за процесс сбора его данных отвечает обслуживающая сеть абонента. Для стандартизации данных между разными операторами международная ассоциация GSMA ввела стандарт BCE. Согласно ему, данные с CDR должны агрегировать в единый отчет UDR, который впоследствии передается оператору, обслуживающему абонента в домашней сети. На основе этого отчета, домашний оператор выставляет абоненту счет.

## Требуется
Написать сервис, который эмулирует работу коммутатора, то есть создает CDR-файлы. Далее написать UDR-сервис, который агрегирует даннный из CDR-файлов в отчет.

## Требования к CDR-сервису
- 1 CDR = 1 месяц. Тарифицируемый период в рамках задания - 1 год;
- Данные в CDR идут не по порядку, т.е. записи по одному абоненту могут быть в разных частях файла;
- Количество и длительность звонков определяется случайным образом;
- Установленный список абонентов (не менее 10) хранится в локальной БД (h2);
- После генерации CDR, данные о транзакциях пользователя помещаются в соседнюю таблицу этой БД.
- > Пример фрагмента CDR-файла:
  > <br> 02,79876543221, 1709798657, 1709799601
  > <br> 01,79996667755, 1709899870, 1709905806

## Требования к UDR-сервису
- Данные можно брать только из CDR файла. БД с описанием транзакций – тестовая, и доступа к ней, в рамках задания нет;
- Сгенерированные объекты отчета разместить в /reports. Шаблон имени: номер_месяц.json (79876543221_1.json);
- Класс генератора должен содержать методы:
  > + generateReport() – сохраняет все отчеты и выводит в консоль таблицу со всеми абонентами и итоговым временем звонков по всему тарифицируемому периоду каждого абонента;
  > + generateReport(msisdn) – сохраняет все отчеты и выводит в консоль таблицу по одному абоненту и его итоговому времени звонков в каждом месяце;
  > + generateReport(msisdn, month) – сохраняет отчет и выводит в консоль таблицу по одному абоненту и его итоговому времени звонков в указанном месяце.
- > Пример UDR объекта:
- > <br>{
    <br>"msisdn": "79876543221",
    <br>"incomingCall": {
    <br>...."totalTime": "02:12:13"
    <br>},
    <br>"outcomingCall": {
    <br>...."totalTime": "00:02:50"
    <br>}
<br>}


## Общие условия к проекту
- Конечное решение должно быть описано в одном модуле (монолит);
- Допустимо использовать фреймворк Spring и его модули, но приложение НЕ должно запускаться на локальном веб-сервере;
- По умолчанию должен срабатывать метод generateReport();
- В директории /tests должно быть не мене 3 unit тестов;
- К ключевым классам добавить javadoc описание;
- Конечное решение размещаете на репозитории в github в виде проекта и jar файла с зависимостями;ё
- В репозитории разместить md описание задания и вашего решения.

## Описание решения
- Для реализации CDR-сервиса были написаны классы CDRGenerator, CDRobject и сам CDRService.
  <br>
  <br> CDRGenerator выполняет по сути роль абонента. Он содержит методы, которые возвращают
       случайные номера пользователей, тип их звонков, а также случайное время продолжительности звонка.
  <br>
  <br> CDRObject по сути представляет из себя набор полей CDR-файла с перегруженным методом toString() и getter'ами для удобства маппинга объекта в файл. Ввиду того, что требуется создавать
       CDR-файлы пачками, я написал этот класс.
  <br>
  <br> CDRService, в свою очередь, и выполняет роль этого сервиса. Потому в нем сожержатся методы генерации транзакций абонентов и запись их в файлы. Также он отвечает за запись
       данных абонентов (их номеров) и данных о транзакциях каждого абонента в базу данных, если конкретнее, то он вызывает методы класса DBConnection, о котором написано ниже.
  <br>
- DBConnection
  <br>
  <br> DBConnection представляет из себя класс, который отвечает за создание соединения с БД и реализацию тех самых методов, которые используются для записи. Он имеет
       три публичных метода, с помощью которых осуществляется запись из класса CDRService:
  >  + getUsersNumbers используется для получения списка номеров пользователей из БД, если они там есть, то CDRService использует их для создания транзакций по ним, если нет,
       то он вызывает метод phoneNumbersGenerator класса CDRGenerator, который генерирует "новых пользователей" (а точнее их номера телефонов) и записывает в БД с помощью метода 
       insertUsersData класса DBConnection.
  >  + insertUsersData как уже написано выше, нужен для записи номеров телефонов в БД.
  >  + insertUsersTransactionsData записывает данные о транзакциях каждого пользователя, при этом сопоставляет абонента с его транзакциями через id пользователя и
       ForeignKey userId в таблице UsersTransactions, так что каждая транзакция однозначно связана с пользователем, ее совершившем.
  <br>
- Для реализации UDR-сервиса ввиду объемности задачи потребовалось также несколько классов: IncomingCall, OutcomingCall, UDRObject и UDRService.
  <br>
  <br> IncomingCall и OutcomingCall представляют из себя классы, которые дают возможность создавать объекты, которые агрегируют в себя суммарное время пользователей за определенные
       промежутки времени (в данном случае месяц). Помимо getter'а и adder'а (который нужен как раз таки для "приплюсовывания" продолжительности новых транакций абонента)
       присутствует метод getStrTotalTime, который переводит Unix time в строку формата "HH:MM:SS".
  <br>
  <br> UDRObject создан с теми же целями, что и CDRObject, более того, структурой своих полей он повторяет структуру UDR-отчет.json, что было сделано также для более удобного
       маппинга в файл расширения .json.
  <br>
  <br> UDRService имплементирует в себе все методы по созданию UDR-объектов и последующей записи их в json'ы. Также он отвечает за создание и вывод таблиц с данными в консоль.
       За это отвечают три публичных метода: generateReport(), generateReport(msisdn), generateReport(msisdn, month):
  >  + generateReport() срабатывает по умолчанию и выводит в консоль таблицу c транзакциями каждого абонента за каждый месяц и записывает нужные json'ы в /reports. 
  >  + generateReport(msisdn) не срабатывает по умолчанию, но может выводить в консоль таблицу с транзакциями конкретного абонента за все месяцы и сохраняет json's в /reports.
  >  + generateReport(msisdn, month) тоже не срабатывает, но может выводить в консоль таблицу с транзакциями конкретного абонента за конкретный месяц в году и сохраняет
       json'ы в /reports.
  <br>
- Main является точкой входа.


















  


