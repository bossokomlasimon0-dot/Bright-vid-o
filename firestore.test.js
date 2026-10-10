const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot create publication", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("publications").doc("pub_1").set({
      title: "Test Video",
      type: "video",
      category: "Technologie",
      externalUrl: "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      authorId: ALICE_UID,
      status: "pending",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can create publication with status pending", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("publications").doc("pub_1").set({
      title: "Tutoriel Android Jetpack Compose",
      type: "tutorial",
      category: "Tutoriels",
      externalUrl: "https://youtube.com/watch?v=12345",
      authorId: ALICE_UID,
      status: "pending",
      createdAt: new Date(),
    })
  );
});

test("Normal user: cannot self-approve publication", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("publications").doc("pub_2").set({
      title: "Self Approved Video",
      type: "video",
      category: "Divertissement",
      externalUrl: "https://youtube.com/watch?v=12345",
      authorId: ALICE_UID,
      status: "approved",
      createdAt: new Date(),
    })
  );
});

test("Admin: can create publication directly as approved", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "bossokomlasimon0@gmail.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    adminDb.collection("publications").doc("pub_admin").set({
      title: "Bienvenue sur Bright Video",
      type: "article",
      category: "Actualités",
      externalUrl: "https://brightvideo.pages.dev/welcome",
      authorId: ADMIN_UID,
      status: "approved",
      createdAt: new Date(),
    })
  );
});

test("Public user: can read approved publications", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("publications").doc("pub_app").set({
      title: "Vidéo Publique",
      type: "video",
      category: "Musique",
      externalUrl: "https://youtube.com/watch?v=abcde",
      authorId: ALICE_UID,
      status: "approved",
      createdAt: new Date(),
    });
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(
    unauthDb.collection("publications").doc("pub_app").get()
  );
});

test("Public user: cannot read pending publications of another user", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("publications").doc("pub_pend").set({
      title: "Vidéo En Attente",
      type: "video",
      category: "Musique",
      externalUrl: "https://youtube.com/watch?v=abcde",
      authorId: ALICE_UID,
      status: "pending",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("publications").doc("pub_pend").get()
  );
});
