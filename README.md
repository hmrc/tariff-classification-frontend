
# Tariff Classification Front End

The Front End for the internal Operator Service for reviewing & determining BTI applications


### Running

##### To run this Service you will need:

1) [Service Manager](https://github.com/hmrc/service-manager) Installed
2) [SBT](https://www.scala-sbt.org) Version `>0.13.13` Installed

##### Starting the application:
 
1) Run Assets Frontend: `sm --start ASSETS_FRONTEND -r 4.5.0`
2) Start [Binding Tariff Classification](https://github.com/hmrc/binding-tariff-classification) Using `sm --start BINDING_TARIFF_CLASSIFICATION -f`
3) Start Stride Auth Frontend Using `sm --start STRIDE_AUTH_FRONTEND -r`
4) Start Stride Auth Using `sm --start STRIDE_AUTH -r`
5) Start Stride Auth IDP Stub Using `sm --start STRIDE_IDP_STUB -r`
6) Start Email `sm --start EMAIL -r`
7) Start Mailgun Stub `sm --start MAILGUN_STUB -r`
8) Start Email Renderer `sm --start HMRC_EMAIL_RENDERER -r`

Finally Run `sbt run` to boot the app

Open `http://localhost:9000/tariff-classification-frontend`
 
See [Binding Tariff Classification](https://github.com/hmrc/binding-tariff-classification) for info on how to set up test data

##### Starting With Service Manager

This application runs on port 9581

Run `sm --start TARIFF_CLASSIFICATION_FRONTEND -r`

Open `http://localhost:9581/tariff-classification`

### Testing

Run `./run_all_tests.sh`

or `sbt test it:test`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
