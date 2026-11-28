const test = require('node:test');
const assert = require('node:assert/strict');
const {retryAtFromResponse,QuotaError} = require('./quota');
test('uses Google RetryInfo and never retries before the provider delay',()=>{
  assert.equal(retryAtFromResponse({error:{details:[{'@type':'type.googleapis.com/google.rpc.RetryInfo',retryDelay:'20.25s'}]}},null,1000),21250);
});
test('uses Retry-After and a conservative default when no valid delay exists',()=>{
  assert.equal(retryAtFromResponse({},'60',1000),61000);
  assert.equal(retryAtFromResponse({},null,1000),3601000);
  assert.equal(retryAtFromResponse({},'999999999',1000),1000+999999999000);
  assert.equal(new QuotaError(42).code,'PROVIDER_QUOTA');
});
