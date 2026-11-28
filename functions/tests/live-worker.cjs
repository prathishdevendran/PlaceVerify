const {initializeApp}=require('firebase-admin/app'); const {getFirestore,FieldValue}=require('firebase-admin/firestore');
if (process.env.FIRESTORE_EMULATOR_HOST !== '127.0.0.1:8080') throw Error('A local Firestore emulator is required.');
initializeApp({projectId:process.env.GCLOUD_PROJECT || 'demo-placeverify'});const db=getFirestore();
(async()=>{const kit=db.collection('preparationKits').doc('local-worker-test'); const job=db.collection('researchJobs').doc();
await kit.set({companyId:'local-test',collegeId:'local-college',companyName:'TCS',role:'Software Developer',status:'QUEUED',jobId:job.id,updatedAt:FieldValue.serverTimestamp()});
await job.create({kitId:kit.id,companyId:'local-test',companyName:'TCS',role:'Software Developer',collegeId:'local-college',requestedBy:'local-test',status:'QUEUED',createdAt:FieldValue.serverTimestamp()});
let d; const start=Date.now();while(Date.now()-start<470000){await new Promise(r=>setTimeout(r,5000)); d=(await kit.get()).data();if(['FAILED','PARTIAL','COMPLETED'].includes(d.status))break;}
const questions=await kit.collection('interviewQuestions').where('jobId','==',job.id).get();const exp=await kit.collection('interviewExperiences').where('jobId','==',job.id).get();const j=(await job.get()).data();
console.log('WORKER_STATUS='+(d?.status||'TIMEOUT')); console.log('API_CALLS='+(j.apiCalls||0));console.log('REPORTED_QUESTIONS='+questions.docs.filter(q=>q.data().origin==='Previously Reported').length); console.log('PRACTICE_QUESTIONS='+questions.docs.filter(q=>q.data().origin==='AI Generated Practice').length); console.log('EXPERIENCES='+exp.size);
})().catch(e=>{console.log('WORKER_TEST_FAILED='+e.message);process.exitCode=1});
