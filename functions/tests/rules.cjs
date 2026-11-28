const {initializeTestEnvironment, assertFails, assertSucceeds} = require('@firebase/rules-unit-testing');
const {doc, setDoc, getDoc, deleteDoc, updateDoc, collection, getDocs, query, where, Timestamp} = require('firebase/firestore');
const fs = require('node:fs');
const test = require('node:test');
const assert = require('node:assert/strict');
let env;
test.before(async () => {
  env = await initializeTestEnvironment({projectId:'demo-placeverify',firestore:{rules:fs.readFileSync('../firestore.rules','utf8')}});
  await env.withSecurityRulesDisabled(async context => {
    const db = context.firestore();
    for (const path of ['users/new-student','applications/app','misinformationReports/r']) await deleteDoc(doc(db,path));
    await setDoc(doc(db,'companies/a'),{collegeId:'a',approved:true,name:'Company'});
    await setDoc(doc(db,'companies/b'),{collegeId:'b',approved:true,name:'Other'});
    await setDoc(doc(db,'preparationKits/k'),{collegeId:'a'});
    await setDoc(doc(db,'preparationKits/k/interviewQuestions/q'),{text:'Question'});
  });
});
test.after(async () => { if (env) await env.cleanup(); });
test('college access and trusted writes', async () => {
  const a = env.authenticatedContext('alice',{collegeId:'a',role:'STUDENT'}).firestore();
  const b = env.authenticatedContext('bob',{collegeId:'b',role:'STUDENT'}).firestore();
  const unauth = env.unauthenticatedContext().firestore();
  await assertSucceeds(getDoc(doc(a,'companies/a')));
  await assertFails(getDoc(doc(b,'companies/a')));
  await assertFails(getDoc(doc(unauth,'companies/a')));
  await assertFails(setDoc(doc(a,'companies/new'),{collegeId:'a',approved:true}));
  await assertFails(setDoc(doc(a,'preparationKits/k/interviewQuestions/fake'),{text:'fake'}));
  await assertSucceeds(getDoc(doc(a,'preparationKits/k/interviewQuestions/q')));
  await assertFails(getDoc(doc(b,'preparationKits/k/interviewQuestions/q')));
});
test('progress is private to its owner', async () => {
  const a = env.authenticatedContext('alice',{collegeId:'a'}).firestore();
  const b = env.authenticatedContext('bob',{collegeId:'a'}).firestore();
  const progress = doc(a,'studentPreparationProgress/alice/questions/q');
  await assertSucceeds(setDoc(progress,{bookmarked:true,completed:false,updatedAt:Timestamp.now()}));
  await assertFails(getDoc(doc(b,'studentPreparationProgress/alice/questions/q')));
  await assertFails(setDoc(doc(b,'studentPreparationProgress/alice/questions/q'),{bookmarked:true,completed:true,updatedAt:Timestamp.now()}));
});

test('student profile creation is restricted to its authenticated owner and student fields', async () => {
  const a = env.authenticatedContext('new-student',{email:'alice@example.test'}).firestore();
  const b = env.authenticatedContext('other',{email:'other@example.test'}).firestore();
  const anon = env.unauthenticatedContext().firestore();
  const ref = doc(a,'users/new-student');
  const valid = {uid:'new-student',name:'Alice',email:'alice@example.test',role:'STUDENT',institution:'College',createdAt:1};
  await assertFails(setDoc(ref,{...valid,role:'SUPER_ADMIN'}));
  await assertFails(setDoc(ref,{...valid,role:'PLACEMENT_OFFICER'}));
  await assertFails(setDoc(ref,{...valid,collegeId:'trusted-college'}));
  await assertFails(setDoc(ref,{...valid,uid:'other'}));
  await assertFails(setDoc(ref,{...valid,email:'other@example.test'}));
  await assertFails(setDoc(doc(b,'users/new-student'),valid));
  await assertFails(setDoc(doc(anon,'users/new-student'),valid));
  await assertSucceeds(setDoc(ref,valid));
  await assertSucceeds(getDoc(ref));
  await assertFails(getDoc(doc(b,'users/new-student')));
  await assertFails(setDoc(ref,{...valid,role:'SUPER_ADMIN'}));
  await assertFails(setDoc(ref,{...valid,createdAt:2}));
  assert.equal((await getDoc(ref)).data().role,'STUDENT');
});
test('privileged claims cannot self-register a student profile', async () => {
  const officer = env.authenticatedContext('officer',{email:'officer@example.test',role:'PLACEMENT_OFFICER'}).firestore();
  await assertFails(setDoc(doc(officer,'users/officer'),{uid:'officer',name:'Officer',email:'officer@example.test',role:'STUDENT',institution:'College',createdAt:1}));
});

test('college reads, queries, and verified records', async () => {
  await env.withSecurityRulesDisabled(async c => {const db=c.firestore();
    await setDoc(doc(db,'opportunities/a'),{collegeId:'a',isVerified:true,title:'Verified'});
    await setDoc(doc(db,'opportunities/b'),{collegeId:'b',isVerified:true,title:'Other'});
    await setDoc(doc(db,'opportunities/draft'),{collegeId:'a',isVerified:false});
    await setDoc(doc(db,'placementDrives/drive'),{collegeId:'a',verified:true});
    await setDoc(doc(db,'researchJobs/job'),{collegeId:'a',requestedBy:'alice'});
    await setDoc(doc(db,'preparationKits/k/interviewExperiences/e'),{text:'Experience'});
    await setDoc(doc(db,'researchProviderState/gemini'),{retryAt:Timestamp.now()});
  });
  const a=env.authenticatedContext('alice',{collegeId:'a',role:'STUDENT'}).firestore();
  const b=env.authenticatedContext('bob',{collegeId:'b',role:'STUDENT'}).firestore();
  const officer=env.authenticatedContext('officer',{collegeId:'a',role:'PLACEMENT_OFFICER'}).firestore();
  const admin=env.authenticatedContext('admin',{collegeId:'a',role:'SUPER_ADMIN'}).firestore();
  await assertSucceeds(getDocs(query(collection(a,'opportunities'),where('collegeId','==','a'),where('isVerified','==',true))));
  await assertFails(getDocs(collection(a,'opportunities')));
  await assertFails(getDoc(doc(a,'opportunities/b')));
  await assertFails(getDoc(doc(a,'opportunities/draft')));
  await assertSucceeds(getDoc(doc(officer,'opportunities/draft')));
  await assertSucceeds(getDoc(doc(admin,'placementDrives/drive')));
  await assertFails(setDoc(doc(officer,'opportunities/a'),{collegeId:'a',isVerified:true,title:'Forged'}));
  await assertFails(updateDoc(doc(admin,'opportunities/a'),{title:'Altered'}));
  await assertFails(setDoc(doc(officer,'companies/new'),{collegeId:'a',approved:true}));
  await assertSucceeds(getDoc(doc(a,'researchJobs/job')));
  await assertFails(getDoc(doc(b,'researchJobs/job')));
  await assertSucceeds(getDoc(doc(a,'preparationKits/k/interviewExperiences/e')));
  await assertFails(getDoc(doc(b,'preparationKits/k/interviewExperiences/e')));
  await assertFails(getDoc(doc(a,'researchProviderState/gemini')));
  await assertFails(setDoc(doc(a,'researchJobs/new'),{collegeId:'a'}));
  await assertSucceeds(getDoc(doc(a,'preparationKits/not-yet-created')));
});

test('personal records and staff actions use owner, role and college', async () => {
  const a=env.authenticatedContext('alice',{collegeId:'a',role:'STUDENT'}).firestore();
  const b=env.authenticatedContext('bob',{collegeId:'a',role:'STUDENT'}).firestore();
  const officer=env.authenticatedContext('officer',{collegeId:'a',role:'PLACEMENT_OFFICER'}).firestore();
  const admin=env.authenticatedContext('admin',{collegeId:'a',role:'SUPER_ADMIN'}).firestore();
  const otherAdmin=env.authenticatedContext('other-admin',{collegeId:'b',role:'SUPER_ADMIN'}).firestore();
  await env.withSecurityRulesDisabled(async c=> {const db=c.firestore();
    await setDoc(doc(db,'notifications/n'),{userId:'alice',collegeId:'a',isRead:false});
    await setDoc(doc(db,'verificationRequests/v'),{collegeId:'a',status:'Pending'});
  });
  await assertSucceeds(setDoc(doc(a,'users/alice/savedPlacements/a'),{savedAt:Timestamp.now()}));
  await assertFails(setDoc(doc(a,'users/alice/savedPlacements/b'),{savedAt:Timestamp.now()}));
  await assertFails(getDoc(doc(b,'users/alice/savedPlacements/a')));
  await assertSucceeds(getDoc(doc(a,'notifications/n')));
  await assertFails(getDoc(doc(b,'notifications/n')));
  await assertSucceeds(updateDoc(doc(a,'notifications/n'),{isRead:true}));
  await assertFails(updateDoc(doc(a,'notifications/n'),{userId:'bob'}));
  const application={userId:'alice',collegeId:'a',opportunityId:'a',opportunityTitle:'Role',companyName:'Company',appliedDate:'today',status:'Applied',ctc:''};
  await assertSucceeds(setDoc(doc(a,'applications/app'),application));
  await assertFails(setDoc(doc(b,'applications/fake'),application));
  await assertFails(setDoc(doc(a,'applications/other'),{...application,opportunityId:'b'}));
  await assertSucceeds(getDoc(doc(officer,'applications/app')));
  await assertFails(getDoc(doc(otherAdmin,'applications/app')));
  const report={collegeId:'a',title:'Suspicious',companyName:'Company',reportedBy:'alice',reason:'Reason',status:'Pending',date:'today'};
  await assertSucceeds(setDoc(doc(a,'misinformationReports/r'),report));
  await assertFails(setDoc(doc(b,'misinformationReports/fake'),report));
  await assertFails(updateDoc(doc(a,'misinformationReports/r'),{status:'Resolved'}));
  await assertSucceeds(updateDoc(doc(officer,'misinformationReports/r'),{status:'Investigating'}));
  await assertFails(updateDoc(doc(officer,'verificationRequests/v'),{status:'Approved'}));
  await assertFails(updateDoc(doc(otherAdmin,'verificationRequests/v'),{status:'Approved'}));
  await assertFails(updateDoc(doc(admin,'verificationRequests/v'),{status:'Approved'}));
  await assertFails(setDoc(doc(a,'unknownCollection/x'),{value:true}));
});

test('profile edits cannot change identity or role', async () => {
  const a=env.authenticatedContext('new-student',{email:'alice@example.test'}).firestore();
  await assertSucceeds(updateDoc(doc(a,'users/new-student'),{name:'New Name'}));
  await assertFails(updateDoc(doc(a,'users/new-student'),{role:'SUPER_ADMIN'}));
  await assertFails(updateDoc(doc(a,'users/new-student'),{collegeId:'a'}));
});

test('no college claim means no college data or provider state access', async () => {
  const user=env.authenticatedContext('no-college',{email:'n@example.test'}).firestore();
  const anonymous=env.unauthenticatedContext().firestore();
  await assertFails(getDoc(doc(user,'opportunities/a')));
  await assertFails(getDoc(doc(user,'companies/a')));
  await assertFails(getDoc(doc(user,'preparationKits/k')));
  await assertFails(getDoc(doc(anonymous,'preparationKits/k')));
  await assertFails(getDoc(doc(user,'researchRateLimits/no-college')));
});
