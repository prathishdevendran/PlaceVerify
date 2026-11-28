// Refuses to touch Firebase unless the Auth and Firestore emulator hosts are explicitly set.
const {initializeApp}=require('firebase-admin/app');
const {getAuth}=require('firebase-admin/auth');
const {getFirestore}=require('firebase-admin/firestore');
if (process.env.FIREBASE_AUTH_EMULATOR_HOST !== '127.0.0.1:9099' || process.env.FIRESTORE_EMULATOR_HOST !== '127.0.0.1:8080') throw Error('Both local emulator hosts are required.');
initializeApp({projectId:'placeverify-845fc'});
const auth=getAuth(), db=getFirestore();
(async()=>{
 for (const [role,email,name] of [['STUDENT','student@placeverify.test','Test Student'],['PLACEMENT_OFFICER','officer@placeverify.test','Test Officer'],['SUPER_ADMIN','admin@placeverify.test','Test Admin']]) {
  let u;try{u=await auth.getUserByEmail(email);}catch(e){if(e.code!=='auth/user-not-found')throw e;u=await auth.createUser({email,password:'PlaceVerifyTest123!',displayName:name});}
  await auth.setCustomUserClaims(u.uid,{role,collegeId:'local-college'});
  await db.collection('users').doc(u.uid).set({uid:u.uid,name,email,role,institution:'Local Test College'},{merge:true});
  console.log(role+' emulator account ready');
 }
})().catch(e=>{console.error(e.message);process.exitCode=1});
