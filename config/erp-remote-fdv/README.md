## Steps to add new RemoteFdVs in the E2E-Testsuite

1. Add the new RemoteFdV in the `config/erp-remote-fdv/config.yaml` similar to the existing ones.
2. Add keystorePassword and truststorePassword of the new RemoteFdV in `secret.ppcs` similar to the existing ones.
3. Add the name of the new RemoteFdV to `FDV_PROFILE` field in `BDD_Testsuite.Jenkinsfile`. 
   The name to add in `BDD_Testsuite.Jenkinsfile` should be identical to the name declared in `config.yaml` in Step 1.