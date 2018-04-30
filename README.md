# AWS Cost API Adapter

This service proxies AWS cost information for a given AWS account.

Environment variables:
* ACCESS_KEY_ID
* SECRET_KEY
* jwk_url
* jwk_kid
* jwk_alg

The REST API can be reached using `/api/v1/aws-costs-adapter` as base Uri. Available endpoints as follows:

```
# request a list of costs, grouped into several resource groups
GET /costs?month=:month&year=:year

# request a list of current existing resource groups
GET /resource-groups
```

Note: Month values are numeric values in the range from 0-11.