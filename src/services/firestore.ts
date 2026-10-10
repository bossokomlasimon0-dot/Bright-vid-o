/**
 * Bright Video - Service Cloud Firestore (JavaScript / TypeScript SDK)
 * Fonctions utilitaires CRUD complètes pour 'users', 'posts' et 'comments'
 * conformément aux types définis dans 'src/types/firestore.ts' et aux règles de sécurité.
 */

import {
  Firestore,
  collection,
  doc,
  getDoc,
  getDocs,
  setDoc,
  addDoc,
  updateDoc,
  deleteDoc,
  query,
  where,
  orderBy,
  limit,
  serverTimestamp,
  increment,
  DocumentData,
  QueryDocumentSnapshot,
} from "firebase/firestore";

import {
  User,
  Post,
  Comment,
  PublicationType,
  ModerationStatus,
} from "../types/firestore";

// Convertisseurs de documents avec typage strict
const postConverter = {
  toFirestore(post: Partial<Post>): DocumentData {
    return { ...post };
  },
  fromFirestore(snapshot: QueryDocumentSnapshot): Post {
    const data = snapshot.data();
    return {
      id: snapshot.id,
      title: data.title ?? "",
      description: data.description ?? "",
      type: data.type ?? "video",
      category: data.category ?? "Technologie",
      externalUrl: data.externalUrl ?? "",
      thumbnailUrl: data.thumbnailUrl ?? "",
      content: data.content ?? "",
      authorId: data.authorId ?? "",
      authorName: data.authorName ?? "Abonné",
      authorAvatarUrl: data.authorAvatarUrl ?? "",
      status: data.status ?? "pending",
      rejectionReason: data.rejectionReason ?? "",
      viewsCount: data.viewsCount ?? 0,
      likesCount: data.likesCount ?? 0,
      commentsCount: data.commentsCount ?? 0,
      tags: data.tags ?? [],
      createdAt: data.createdAt,
      updatedAt: data.updatedAt,
    };
  },
};

const commentConverter = {
  toFirestore(comment: Partial<Comment>): DocumentData {
    return { ...comment };
  },
  fromFirestore(snapshot: QueryDocumentSnapshot): Comment {
    const data = snapshot.data();
    return {
      id: snapshot.id,
      publicationId: data.publicationId ?? "",
      authorId: data.authorId ?? "",
      authorName: data.authorName ?? "Abonné",
      authorAvatarUrl: data.authorAvatarUrl ?? "",
      content: data.content ?? "",
      createdAt: data.createdAt,
      updatedAt: data.updatedAt,
    };
  },
};

// ==========================================
// 1. SERVICES UTILISATEURS ('users')
// ==========================================

/**
 * Récupère le profil d'un utilisateur par son UID
 */
export async function getUser(db: Firestore, userId: string): Promise<User | null> {
  const userRef = doc(db, "users", userId);
  const userSnap = await getDoc(userRef);

  if (!userSnap.exists()) {
    return null;
  }
  return userSnap.data() as User;
}

/**
 * Initialise le profil utilisateur lors de la première connexion Google Sign-In
 */
export async function createUserProfile(
  db: Firestore,
  user: Omit<User, "createdAt" | "updatedAt">
): Promise<void> {
  const userRef = doc(db, "users", user.userId);
  await setDoc(
    userRef,
    {
      ...user,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    },
    { merge: true }
  );
}

/**
 * Met à jour les informations du profil (bio, nom, avatar)
 */
export async function updateUserProfile(
  db: Firestore,
  userId: string,
  data: Partial<Pick<User, "displayName" | "avatarUrl" | "bio">>
): Promise<void> {
  const userRef = doc(db, "users", userId);
  await updateDoc(userRef, {
    ...data,
    updatedAt: serverTimestamp(),
  });
}

// ==========================================
// 2. SERVICES PUBLICATIONS ('posts' / 'publications')
// ==========================================

/**
 * Récupère les publications approuvées pour le flux public (avec filtres optionnels)
 */
export async function getApprovedPosts(
  db: Firestore,
  options?: {
    category?: string;
    type?: PublicationType;
    maxLimit?: number;
  }
): Promise<Post[]> {
  const postsCollection = collection(db, "publications").withConverter(postConverter);

  let q = query(
    postsCollection,
    where("status", "==", "approved"),
    orderBy("createdAt", "desc"),
    limit(options?.maxLimit ?? 50)
  );

  if (options?.category && options.category !== "Tous") {
    q = query(q, where("category", "==", options.category));
  }

  if (options?.type) {
    q = query(q, where("type", "==", options.type));
  }

  const snapshot = await getDocs(q);
  return snapshot.docs.map((d) => d.data());
}

/**
 * Récupère une publication spécifique par son ID
 */
export async function getPostById(db: Firestore, postId: string): Promise<Post | null> {
  const postRef = doc(db, "publications", postId).withConverter(postConverter);
  const snapshot = await getDoc(postRef);

  if (!snapshot.exists()) {
    return null;
  }
  return snapshot.data();
}

/**
 * Récupère toutes les publications soumises par un utilisateur spécifique
 */
export async function getUserPosts(db: Firestore, authorId: string): Promise<Post[]> {
  const postsCollection = collection(db, "publications").withConverter(postConverter);
  const q = query(
    postsCollection,
    where("authorId", "==", authorId),
    orderBy("createdAt", "desc")
  );

  const snapshot = await getDocs(q);
  return snapshot.docs.map((d) => d.data());
}

/**
 * Récupère les publications en attente de modération (Réservé administrateur)
 */
export async function getPendingPosts(db: Firestore): Promise<Post[]> {
  const postsCollection = collection(db, "publications").withConverter(postConverter);
  const q = query(
    postsCollection,
    where("status", "==", "pending"),
    orderBy("createdAt", "asc")
  );

  const snapshot = await getDocs(q);
  return snapshot.docs.map((d) => d.data());
}

/**
 * Soumet une nouvelle publication multimédia (en attente de modération ou directe si admin)
 */
export async function createPost(
  db: Firestore,
  postData: Omit<
    Post,
    | "id"
    | "status"
    | "createdAt"
    | "updatedAt"
    | "viewsCount"
    | "likesCount"
    | "commentsCount"
  >,
  isAdmin: boolean = false
): Promise<string> {
  const postsCollection = collection(db, "publications");
  const initialStatus: ModerationStatus = isAdmin ? "approved" : "pending";

  const docRef = await addDoc(postsCollection, {
    ...postData,
    status: initialStatus,
    viewsCount: 0,
    likesCount: 0,
    commentsCount: 0,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  });

  return docRef.id;
}

/**
 * Action de modération par l'administrateur (Approuver ou Refuser avec motif)
 */
export async function moderatePost(
  db: Firestore,
  postId: string,
  approved: boolean,
  rejectionReason?: string
): Promise<void> {
  const postRef = doc(db, "publications", postId);
  const updates: Record<string, any> = {
    status: approved ? "approved" : "rejected",
    updatedAt: serverTimestamp(),
  };

  if (!approved && rejectionReason) {
    updates.rejectionReason = rejectionReason;
  }

  await updateDoc(postRef, updates);
}

/**
 * Incrémente le compteur de vues d'une publication
 */
export async function incrementPostViews(db: Firestore, postId: string): Promise<void> {
  const postRef = doc(db, "publications", postId);
  await updateDoc(postRef, {
    viewsCount: increment(1),
    updatedAt: serverTimestamp(),
  });
}

/**
 * Supprime une publication (auteur ou administrateur)
 */
export async function deletePost(db: Firestore, postId: string): Promise<void> {
  const postRef = doc(db, "publications", postId);
  await deleteDoc(postRef);
}

// ==========================================
// 3. SERVICES COMMENTAIRES ('comments')
// ==========================================

/**
 * Récupère tous les commentaires d'une publication, triés par date décroissante
 */
export async function getPostComments(db: Firestore, postId: string): Promise<Comment[]> {
  const commentsCollection = collection(db, "publications", postId, "comments").withConverter(
    commentConverter
  );
  const q = query(commentsCollection, orderBy("createdAt", "desc"));
  const snapshot = await getDocs(q);

  return snapshot.docs.map((d) => d.data());
}

/**
 * Ajoute un commentaire et incrémente atomiquement commentsCount sur la publication
 */
export async function addComment(
  db: Firestore,
  postId: string,
  authorId: string,
  authorName: string,
  content: string,
  authorAvatarUrl?: string
): Promise<string> {
  const commentsCollection = collection(db, "publications", postId, "comments");
  const postRef = doc(db, "publications", postId);

  const commentDoc = await addDoc(commentsCollection, {
    publicationId: postId,
    authorId,
    authorName,
    authorAvatarUrl: authorAvatarUrl ?? "",
    content: content.trim(),
    createdAt: serverTimestamp(),
  });

  // Incrément atomique du compteur de commentaires
  await updateDoc(postRef, {
    commentsCount: increment(1),
    updatedAt: serverTimestamp(),
  });

  return commentDoc.id;
}

/**
 * Supprime un commentaire et décrémente le compteur de la publication
 */
export async function deleteComment(
  db: Firestore,
  postId: string,
  commentId: string
): Promise<void> {
  const commentRef = doc(db, "publications", postId, "comments", commentId);
  const postRef = doc(db, "publications", postId);

  await deleteDoc(commentRef);
  await updateDoc(postRef, {
    commentsCount: increment(-1),
    updatedAt: serverTimestamp(),
  });
}

// ==========================================
// 4. SERVICES LIKES ('likes')
// ==========================================

/**
 * Bascule l'état d'un like (aime ou retire son like) avec mise à jour atomique du compteur
 */
export async function togglePostLike(
  db: Firestore,
  postId: string,
  userId: string
): Promise<boolean> {
  const likeRef = doc(db, "publications", postId, "likes", userId);
  const postRef = doc(db, "publications", postId);
  const likeSnap = await getDoc(likeRef);

  if (likeSnap.exists()) {
    // Retirer le like
    await deleteDoc(likeRef);
    await updateDoc(postRef, {
      likesCount: increment(-1),
      updatedAt: serverTimestamp(),
    });
    return false;
  } else {
    // Ajouter le like
    await setDoc(likeRef, {
      userId,
      publicationId: postId,
      createdAt: serverTimestamp(),
    });
    await updateDoc(postRef, {
      likesCount: increment(1),
      updatedAt: serverTimestamp(),
    });
    return true;
  }
}

/**
 * Vérifie si un utilisateur a déjà aimé une publication
 */
export async function checkIsPostLiked(
  db: Firestore,
  postId: string,
  userId: string
): Promise<boolean> {
  const likeRef = doc(db, "publications", postId, "likes", userId);
  const likeSnap = await getDoc(likeRef);
  return likeSnap.exists();
}
