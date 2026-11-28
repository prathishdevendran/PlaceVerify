const { initializeApp } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const { getFirestore } = require('firebase-admin/firestore');
initializeApp({projectId:'demo-placeverify'});
const db = getFirestore(); const auth = getAuth();
async function bearer(email,password) {
  const r=await fetch('http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=fake-key',{method:'POST',headers:{'content-type':'application/json'},body:JSON.stringify({email,password,returnSecureToken:true})});
  const j=await r.json(); if(!r.ok)throw Error('Auth emulator failed '+r.status); return j.idToken;
}
async function callable(name,token,data) {
 const r=await fetch('http://127.0.0.1:5001/demo-placeverify/us-central1/'+name,{method:'POST',headers:{'content-type':'application/json','authorization':'Bearer '+token,'X-Firebase-AppCheck': 'eyJhbGciOiJub25lIn0.eyJzdWIiOiJlbXVsYXRvci10ZXN0In0.'},body:JSON.stringify({data})});
 const j=await r.json(); if(!r.ok)throw Error(name+' returned '+r.status+' '+(j.error?.status||''));return j.result;
}
(async()=>{
 const password='Local-test-password-472!';
 const officer=await auth.createUser({email:'officer@local.test',password});
 const student=await auth.createUser({email:'student@local.test',password});
 await auth.setCustomUserClaims(officer.uid,{collegeId:'college-local',role:'PLACEMENT_OFFICER'});
 await auth.setCustomUserClaims(student.uid,{collegeId:'college-local',role:'STUDENT'});
 const ot=await bearer('officer@local.test',password);const st=await bearer('student@local.test',password);
 const drive=await callable('publishPlacementDrive',ot,{companyName:'TCS',role:'Software Developer'});
 const company=await db.collection('companies').doc(drive.companyId).get();
 console.log('OFFICER_PUBLISHED='+Boolean(company.exists));
 const kit=await callable('requestPreparationKit',st,{companyId:drive.companyId,role:'Software Developer'});
 const second=await callable('requestPreparationKit',st,{companyId:drive.companyId,role:'Software Developer'});
 console.log('SAME_JOB_REUSED='+(kit.jobId===second.jobId)); if (process.env.QUEUE_ONLY === '1') return;
 const start=Date.now();let doc;
 while(Date.now()-start<480000) { await new Promise(r=>setTimeout(r,4000)); doc=await db.collection('preparationKits').doc(kit.kitId).get(); const status=doc.data()?.status; if(['FAILED','PARTIAL','COMPLETED'].includes(status))break; }
 const data=doc?.data()||{};console.log('KIT_STATUS='+(data.status||'TIMEOUT'));
 const q=await db.collection('preparationKits').doc(kit.kitId).collection('interviewQuestions').where('jobId','==',data.jobId||'').get();
 const e=await db.collection('preparationKits').doc(kit.kitId).collection('interviewExperiences').where('jobId','==',data.jobId||'').get();
 console.log('REPORTED_QUESTIONS='+q.docs.filter(x=>x.data().origin==='Previously Reported').length);
 console.log('PRACTICE_QUESTIONS='+q.docs.filter(x=>x.data().origin==='AI Generated Practice').length);
 console.log('EXPERIENCES='+e.size);
})().catch(e=>{ console.log('TEST_FAILED='+e.message);process.exitCode=1; });

