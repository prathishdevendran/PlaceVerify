// Provider errors may contain sensitive details: expose only a bounded retry timestamp.
function retryAtFromResponse(body, retryAfter, now = Date.now()) {
  const detail = body?.error?.details?.find(x => typeof x?.['@type'] === 'string' && x['@type'].endsWith('google.rpc.RetryInfo'));
  const duration = typeof detail?.retryDelay === 'string' ? detail.retryDelay.match(/^(\d+(?:\.\d+)?)s$/) : null;
  let ms = duration ? Number(duration[1]) * 1000 : NaN;
  if (!Number.isFinite(ms) && retryAfter) {
    const seconds = Number(retryAfter);
    ms = Number.isFinite(seconds) ? seconds * 1000 : Date.parse(retryAfter) - now;
  }
  // A missing or invalid provider window gets a conservative one-hour cooldown.
  if (!Number.isFinite(ms) || ms <= 0) ms = 3600000;
  return Math.min(now + Math.max(Math.ceil(ms), 1000), 253402300799999);
}
class QuotaError extends Error {
  constructor(retryAt) { super('Research is temporarily unavailable because the provider quota is exhausted.'); this.retryAt = retryAt; this.code = 'PROVIDER_QUOTA'; }
}
module.exports = { retryAtFromResponse, QuotaError };
