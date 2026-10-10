/**
 * Bright Video - Schéma et Définitions TypeScript pour Cloud Firestore
 * Utilisé pour structurer les données Firebase, assurer le typage strict
 * et garantir la synchronisation avec les règles de sécurité firestore.rules.
 */

import { Timestamp } from "firebase/firestore";

/**
 * Types de contenus multimédias pris en charge sur Bright Video
 * Aucun stockage direct de fichiers vidéo dans Firebase : utilisation d'URLs externes légales
 */
export type PublicationType =
  | 'video'
  | 'tutorial'
  | 'article'
  | 'music'
  | 'download'
  | 'document'
  | 'photo';

/**
 * Statuts du flux de modération de la plateforme
 * 'pending'  : En attente d'approbation par l'administrateur
 * 'approved' : Approuvé et visible publiquement sur le flux
 * 'rejected' : Refusé avec motif communiqué à l'auteur
 */
export type ModerationStatus = 'pending' | 'approved' | 'rejected';

/**
 * Rôles utilisateurs du système
 */
export type UserRole = 'user' | 'admin';

/**
 * Interface pour la collection 'users' (/users/{userId})
 */
export interface User {
  /** Identifiant unique UID Firebase Authentication */
  userId: string;
  /** Adresse email du compte */
  email: string;
  /** Nom complet affiché publiquement */
  displayName: string;
  /** URL de l'avatar de profil */
  avatarUrl?: string;
  /** Biographie courte (max 500 caractères) */
  bio?: string;
  /** Rôle de l'utilisateur sur la plateforme */
  role: UserRole;
  /** Nombre d'abonnés à la chaîne */
  subscriberCount: number;
  /** Horodatage de création du compte */
  createdAt: Timestamp;
  /** Horodatage de dernière mise à jour */
  updatedAt?: Timestamp;
}

/**
 * Interface pour la collection 'posts' / 'publications' (/publications/{publicationId})
 */
export interface Post {
  /** Identifiant unique du document */
  id?: string;
  /** Titre de la publication (min 2, max 150 caractères) */
  title: string;
  /** Courte description ou résumé (max 2000 caractères) */
  description?: string;
  /** Type de contenu multimédia */
  type: PublicationType;
  /** Catégorie thématique */
  category: string;
  /**
   * Lien externe validé vers la ressource :
   * YouTube, Vimeo, Dailymotion, GitHub, Soundcloud, lien de téléchargement direct, etc.
   */
  externalUrl: string;
  /** URL d'illustration ou miniature (thumbnail) */
  thumbnailUrl?: string;
  /** Corps de texte complet pour les articles ou tutoriels pas à pas */
  content?: string;
  /** UID de l'auteur de la publication */
  authorId: string;
  /** Nom affiché de l'auteur */
  authorName?: string;
  /** Avatar de l'auteur */
  authorAvatarUrl?: string;
  /** Statut de modération */
  status: ModerationStatus;
  /** Motif du rejet si status === 'rejected' */
  rejectionReason?: string;
  /** Nombre de vues */
  viewsCount: number;
  /** Nombre de mentions J'aime */
  likesCount: number;
  /** Nombre de commentaires */
  commentsCount: number;
  /** Liste de tags et mots-clés associés (max 10) */
  tags?: string[];
  /** Date et heure de soumission */
  createdAt: Timestamp;
  /** Date et heure de dernière modification */
  updatedAt?: Timestamp;
}

/**
 * Interface pour la sous-collection 'comments' (/publications/{publicationId}/comments/{commentId})
 */
export interface Comment {
  /** Identifiant unique du commentaire */
  id?: string;
  /** Référence vers l'identifiant de la publication */
  publicationId: string;
  /** UID de l'auteur du commentaire */
  authorId: string;
  /** Nom de l'auteur du commentaire */
  authorName: string;
  /** URL de l'avatar de l'auteur */
  authorAvatarUrl?: string;
  /** Contenu textuel du commentaire (1 à 1000 caractères) */
  content: string;
  /** Horodatage de publication du commentaire */
  createdAt: Timestamp;
  /** Horodatage de dernière modification */
  updatedAt?: Timestamp;
}

/**
 * Interface pour la collection 'categories' (/categories/{categoryId})
 */
export interface Category {
  /** Identifiant ou clé unique (ex: 'technologie', 'tutoriels') */
  id: string;
  /** Nom affiché en français (ex: 'Technologie', 'Tutoriels') */
  name: string;
  /** Identifiant lisible en URL */
  slug: string;
  /** Description de la thématique */
  description?: string;
  /** Nom de l'icône Material Symbols associée */
  icon?: string;
  /** Ordre d'affichage dans la barre de navigation */
  order?: number;
}

/**
 * Interface pour la sous-collection 'likes' (/publications/{publicationId}/likes/{userId})
 */
export interface Like {
  /** UID de l'utilisateur qui a aimé */
  userId: string;
  /** Identifiant de la publication */
  publicationId: string;
  /** Horodatage du like */
  createdAt: Timestamp;
}

/**
 * Interface pour la collection 'subscriptions' (/subscriptions/{subscriptionId})
 */
export interface Subscription {
  /** Identifiant unique de l'abonnement */
  id?: string;
  /** UID de l'abonné */
  subscriberId: string;
  /** UID du créateur / chaîne suivie */
  channelUserId: string;
  /** Nom du créateur */
  channelName: string;
  /** Date d'abonnement */
  createdAt: Timestamp;
}

/**
 * Interface pour la collection 'reports' (/reports/{reportId})
 * Utilisée pour la modération des signalements
 */
export interface Report {
  /** Identifiant du signalement */
  id?: string;
  /** Publication ciblée */
  publicationId: string;
  /** Titre de la publication signalée */
  publicationTitle?: string;
  /** UID de l'utilisateur ayant signalé */
  reportedByUserId: string;
  /** Email du rapporteur */
  reporterEmail?: string;
  /** Motif du signalement (spam, contenu inapproprié, lien brisé, etc.) */
  reason: string;
  /** Précisions apportées par l'utilisateur */
  details?: string;
  /** Statut du traitement par l'administrateur */
  status: 'pending' | 'resolved' | 'dismissed';
  /** Horodatage du signalement */
  createdAt: Timestamp;
  /** Horodatage de résolution */
  updatedAt?: Timestamp;
}
