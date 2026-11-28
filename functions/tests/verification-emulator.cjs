const assert = require('node:assert/strict');
const {initializeApp} = require('firebase-admin/app');
const {getAuth} = require('firebase-admin/auth');
const {getFirestore} = require('firebase-admin/firestore');
if (process.env.FIRESTORE_EMULATOR_HOST !== '127.0.0.1:8081' || process.env.FIREBASE_AUTH_EMULATOR_HOST !== '127.0.0.1:9098') throw Error('Isolated emulators required');
initializeApp({projectId:'demo-placeverify'});
const auth=getAuth(), db=getFirestore();
const appCheck='eyJhbGciOiJub25lIn0.eyJzdWIiOiJlbXVsYXRvci10ZXN0In0.';
async function user(name, role, collegeId) {
 const email=`${name}@example.test`, password='LocalTesting123!';
 const u=await auth.createUser({email,password}); await auth.setCustomUserClaims(u.uid,{role,collegeId});
 const r=await fetch('http://127.0.0.1:9098/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=emulator',{method:'POST',headers:{'content-type':'application/json'},body:JSON.stringify({email,password,returnSecureToken:true})});
 return (await r.json()).idToken;
}
async function call(name, token, data) {
 const r=await fetch(`http://127.0.0.1:5002/demo-placeverify/us-central1/${name}`,{method:'POST',headers:{'content-type':'application/json','authorization':`Bearer ${token}`,'X-Firebase-AppCheck':appCheck},body:JSON.stringify({data})});
 return {status:r.status,body:await r.json()};
}
(async()=>{
 const [officer,student,admin,otherAdmin]=await Promise.all([user('officer','PLACEMENT_OFFICER','a'),user('student','STUDENT','a'),user('admin','SUPER_ADMIN','a'),user('other','SUPER_ADMIN','b')]);
 const payload={companyName:'Testing Company',role:'Developer',details:'Details'};
 assert.notEqual((await call('publishPlacementDrive',student,payload)).status,200);
 const submitted=await call('publishPlacementDrive',officer,payload); assert.equal(submitted.status,200,JSON.stringify(submitted.body));
 const {driveId,companyId}=submitted.body.result; let d=(await db.doc(`placementDrives/${driveId}`).get()).data();
 assert.equal(d.status,'PENDING'); assert.equal(d.verified,false); assert.equal((await db.doc(`companies/${companyId}`).get()).exists,false); assert.equal((await db.doc(`opportunities/${driveId}`).get()).exists,false);
 assert.notEqual((await call('reviewPlacementDrive',officer,{driveId,decision:'APPROVED',evidence:'Officer'})).status,200);
 assert.notEqual((await call('reviewPlacementDrive',otherAdmin,{driveId,decision:'APPROVED',evidence:'Wrong college'})).status,200);
 assert.notEqual((await call('reviewPlacementDrive',admin,{driveId,decision:'APPROVED'})).status,200);
 const reviewed=await call('reviewPlacementDrive',admin,{driveId,decision:'APPROVED',evidence:'Verified by admin'}); assert.equal(reviewed.status,200,JSON.stringify(reviewed.body));
 d=(await db.doc(`placementDrives/${driveId}`).get()).data(); assert.equal(d.status,'APPROVED'); assert.equal(d.verificationEvidence,'Verified by admin'); assert.ok(d.reviewedBy); assert.ok(d.reviewedAt); assert.equal((await db.doc(`opportunities/${driveId}`).get()).get('isVerified'),true);
 assert.notEqual((await call('reviewPlacementDrive',admin,{driveId,decision:'REJECTED',rejectionReason:'again'})).status,200);
 const second=await call('publishPlacementDrive',officer,{companyName:'Other Company',role:'Engineer'}); const secondId=second.body.result.driveId;
 assert.equal((await call('reviewPlacementDrive',admin,{driveId:secondId,decision:'REJECTED',rejectionReason:'Insufficient evidence'})).status,200);
 assert.equal((await db.doc(`placementDrives/${secondId}`).get()).get('rejectionReason'),'Insufficient evidence'); assert.equal((await db.doc(`opportunities/${secondId}`).get()).exists,false);
 console.log('PASS: submission, role and college denial, approval evidence, immutable review, rejection');
})().catch(e=>{console.error(e);process.exitCode=1});
