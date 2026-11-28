const crypto = require('node:crypto');
const norm = s => String(s || '').normalize('NFKC').toLowerCase().replace(/[^\p{L}\p{N}]+/gu, ' ').trim().replace(/\s+/g, ' ');
const id = s => crypto.createHash('sha256').update(s).digest('hex');
function parsePractice(raw, companyId, role) {
  const cleaned = String(raw || '').replace(/^\s*```(?:json)?\s*/i, '').replace(/\s*```\s*$/, '');
  let values;
  try { values = JSON.parse(cleaned); } catch (_) { return []; }
  if (!Array.isArray(values)) return [];
  return values.filter(x => x && typeof x === 'object' && typeof x.text === 'string' && x.text.trim().length >= 10)
    .map(x => ({text:x.text.trim().slice(0,1500),category:typeof x.category === 'string' ? x.category.slice(0,100) : '',difficulty:typeof x.difficulty === 'string' ? x.difficulty.slice(0,30) : '',round:typeof x.round === 'string' ? x.round.slice(0,100) : '',answer:typeof x.answer === 'string' ? x.answer.slice(0,3000) : '',origin:'AI Generated Practice',companyId,role}));
}
function uniqueQuestions(items) {
  const result = [];
  for (const q of items) {
    const key = norm(q.text); if (key.length < 10) continue;
    const words = new Set(key.split(' '));
    const found = result.find(item => {
      const other = new Set(norm(item.text).split(' '));
      const intersection = [...words].filter(w => other.has(w)).length;
      return key === norm(item.text) || (words.size >= 5 && intersection / (words.size + other.size - intersection) >= 0.9);
    });
    if (found) {
      if (q.origin === 'Previously Reported' && q.sourceUrl) found.sourceUrls = [...new Set([...(found.sourceUrls || []), q.sourceUrl])];
    } else result.push({...q, sourceUrls: q.origin === 'Previously Reported' && q.sourceUrl ? [q.sourceUrl] : []});
  }
  return result.map(q => ({...q, sourceCount: q.sourceUrls.length}));
}
function groundedItems(candidate, companyId, role) {
  const text = (candidate.content?.parts || []).map(p => p.text || '').join('');
  const chunks = candidate.groundingMetadata?.groundingChunks || [];
  const supports = candidate.groundingMetadata?.groundingSupports || [];
  const results = [];
  let offset = 0;
  // Citations are assigned only when the provider identifies a supporting text span.
  for (const line of text.split('\n')) {
    const start = offset; const end = start + Buffer.byteLength(line, 'utf8'); offset = end + 1;
    const match = line.match(/^\s*(QUESTION|EXPERIENCE)\s*:\s*(.+)/i);
    if (!match) continue;
    const contentStart = start + Buffer.byteLength(line.slice(0, line.indexOf(match[2])), 'utf8');
    const sources = supports.filter(s => (s.segment?.startIndex ?? 0) <= contentStart && (s.segment?.endIndex ?? 0) >= end)
      .flatMap(s => (s.groundingChunkIndices || []).map(i => chunks[i]?.web))
      .filter(w => w && /^https?:\/\//i.test(w.uri || ''));
    if (!sources.length) continue;
    const source = sources[0];
    const item = { text: match[2].trim().slice(0, 1500), companyId, role, sourceUrl: source.uri, sourceTitle: source.title || 'Source', origin: 'Previously Reported' };
    if (item.text.length > 10) results.push({ kind: match[1].toUpperCase(), ...item });
  }
  return results;
}
module.exports = { norm, id, uniqueQuestions, groundedItems, parsePractice };
