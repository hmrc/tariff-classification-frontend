
# tariff-classification-frontend

The frontend for the internal Operational Service for reviewing & answering ATaR applications.

### Running

##### To run this Service you will need:

1) [Service Manager](https://github.com/hmrc/service-manager) installed
2) [SBT](https://www.scala-sbt.org) Version `>0.13.13` installed
3) [Localstack](https://github.com/localstack/localstack) installed and running on port 4572
4) Create an S3 bucket in localstack by using `awslocal s3 mb s3://digital-tariffs-local` within the localstack container

##### Starting the application:
 
1) Launch dependencies using `sm --start DIGITAL_TARIFF_DEPS -r`
2) Start the backend service [binding-tariff-classification](https://github.com/hmrc/binding-tariff-classification) using `sm --start BINDING_TARIFF_CLASSIFICATION -r`
3) Start the filestore service [binding-tariff-filestore](https://github.com/hmrc/binding-tariff-filestore) using `sm --start BINDING_TARIFF_FILESTORE -r`
4) Start the ruling frontend [binding-tariff-ruling-frontend](https://github.com/hmrc/binding-tariff-ruling-frontend) using `sm --start BINDING_TARIFF_RULING_FRONTEND -r`
5) On Mac OS you must start an older version of the [pdf-generator-service](https://github.com/hmrc/pdf-generator-service):
```
sm --stop PDF_GENERATOR_SERVICE
sm --start PDF_GENERATOR_SERVICE -r 1.20.0
```

Use `sbt run` to boot the app.

This application runs on port 9581.

Open `http://localhost:9581/manage-tariff-classifications`.

You can also run the DIGITAL_TARIFFS profile using `sm --start DIGITAL_TARIFFS -r` and then stop the Service Manager instance of this service using `sm --stop TARIFF_CLASSIFICATION_FRONTEND` before running with sbt.

##### Starting With Service Manager

This application runs on port 9581

Run `sm --start TARIFF_CLASSIFICATION_FRONTEND -r`

Open `http://localhost:9581/manage-tariff-classifications`

### PDF Generator Service

This service requires the installation of some dependencies before it can be run using Service Manager. See [pdf-generator-service](https://github.com/hmrc/pdf-generator-service).

Running PDF Generator Service locally on Mac OSX (currently) requires running an older version.  

Run `sm --start PDF_GENERATOR_SERVICE -r 1.20.0`

### Testing

Run `./run_all_tests.sh`. This also runs Scalastyle and does coverage testing.

or `sbt test it:test` to run the tests only.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
