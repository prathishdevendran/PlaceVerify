const test = require('node:test');
const assert = require('node:assert/strict');
const {norm, uniqueQuestions, groundedItems, parsePractice} = require('./research');
test('normalizes and deduplicates equivalent questions', () => {
  assert.equal(norm('  What is SQL? '), 'what is sql');
  assert.equal(uniqueQuestions([{text:'What is SQL?'},{text:'what is sql!'},{text:'Explain indexing'}]).length, 2);
});
test('drops unsupported or fabricated sources and attaches provider grounding', () => {
  const text = 'QUESTION: Explain binary search\nQUESTION: Unsupported question';
  const candidate = {content:{parts:[{text}]},groundingMetadata:{groundingChunks:[{web:{uri:'https://example.org/a',title:'A'}}],groundingSupports:[{segment:{startIndex:0,endIndex:31},groundingChunkIndices:[0]}]}};
  const items = groundedItems(candidate,'c','r');
  assert.equal(items.length, 1); assert.equal(items[0].sourceUrl,'https://example.org/a');
  candidate.groundingMetadata.groundingChunks[0].web.uri = 'javascript:bad';
  assert.equal(groundedItems(candidate,'c','r').length,0);
});
test('counts distinct supporting links and keeps practice separate from source claims', () => {
  const items = uniqueQuestions([
    {text:'Explain a binary search algorithm',origin:'Previously Reported',sourceUrl:'https://example.com/a'},
    {text:'Explain a binary search algorithm?',origin:'Previously Reported',sourceUrl:'https://example.com/a'},
    {text:'Explain a binary search algorithm!',origin:'Previously Reported',sourceUrl:'https://example.com/b'},
    {text:'Explain a binary search algorithm',origin:'AI Generated Practice'}
  ]);
  assert.equal(items.length, 1);
  assert.equal(items[0].sourceCount, 2);
  assert.equal(items[0].origin, 'Previously Reported');
});
test('rejects partial grounding spans', () => {
  const text = 'QUESTION: Explain a heap data structure';
  const c = {content:{parts:[{text}]},groundingMetadata:{groundingChunks:[{web:{uri:'https://example.com'}}],groundingSupports:[{segment:{startIndex:0,endIndex:18},groundingChunkIndices:[0]}]}};
  assert.equal(groundedItems(c,'c','r').length, 0);
});
test('practice parsing rejects malformed output and validates individual entries', () => {
  assert.deepEqual(parsePractice('{broken', 'c', 'r'), []);
  assert.deepEqual(parsePractice('{"text":"not an array"}', 'c', 'r'), []);
  const result = parsePractice(JSON.stringify([{text:'Explain a binary search algorithm',category:'Coding',answer:'Divide the search range.'},{text:12},null]), 'c', 'r');
  assert.equal(result.length,1);
  assert.equal(result[0].origin,'AI Generated Practice');
  assert.equal(result[0].sourceUrl,undefined);
});
