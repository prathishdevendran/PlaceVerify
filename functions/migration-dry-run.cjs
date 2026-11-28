// Read-only inventory. ADC must refer to the intended project. No role or approval is inferred.
const {initializeApp, applicationDefault} = require('firebase-admin/app');
const {getFirestore} = require('firebase-admin/firestore');
initializeApp({credential:applicationDefault()});
(async () => {
  const db = getFirestore();
  for (const collection of ['users','opportunities','companies','placementDrives']) {
    const rows = await db.collection(collection).select().get();
    console.log(`${collection}: ${rows.size} records; requires administrator review`);
  }
  console.log('Dry run only: no documents or custom claims were changed.');
})().catch(e => { console.error('Inventory failed:',e.message); process.exitCode=1; });
