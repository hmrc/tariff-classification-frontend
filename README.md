
# Tariff Classification Front End

The Front End for the internal Operator Service for reviewing & determining BTI applications


### Running

##### To run this Service you will need:

1) [Service Manager](https://github.com/hmrc/service-manager) Installed
2) [SBT](https://www.scala-sbt.org) Version `>0.13.13` Installed

##### Starting the application:
 
1) Run Assets Frontend: `sm --start ASSETS_FRONTEND -r 4.5.0`
2) Start [Binding Tariff Classification](https://github.com/hmrc/binding-tariff-classification) Using `sm --start BINDING_TARIFF_CLASSIFICATION -f`

Finally Run `sbt run` to boot the app

Open `http://localhost:9000/tariff-classification-frontend`
 
See [Binding Tariff Classification](https://github.com/hmrc/binding-tariff-classification) for info on how to set up test data

##### Starting With Service Manager

This application runs on port 9581

Run `sm --start TARIFF_CALSSIFICATION_FRONTEND -r`

Open `http://localhost:9581/tariff-classification-frontend`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
