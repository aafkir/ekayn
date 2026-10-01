export type ProductImage = { src: string; alt: string };

export type Product = {
  id: string;
  slug: string;
  reference: string;
  name: string;
  description: string;
  shortDescription?: string;
  image: string;
  alt: string;
  images: ProductImage[];
  priceCents: number;
  variants?: { name: string; options: string[] }[];
  specifications?: { label: string; value: string }[];
};

export const products: Product[] = [
  { id: '1', slug: 'eclat-de-tradition', reference: 'TIF–01', name: 'Éclat de tradition', description: 'Perles orangées, médailles et détails colorés composent une pièce élégante, facile à porter au quotidien comme pour une occasion.', image: '/assets/product1/product1.jpg', alt: 'Collier aux perles orange et médailles argentées', priceCents: 1990, images: [{ src: '/assets/product1/product1.jpg', alt: 'Collier Éclat de tradition présenté sur un buste noir' }, { src: '/assets/product1/IMG_9531.JPG', alt: 'Vue d’ensemble du collier Éclat de tradition' }, { src: '/assets/product1/IMG_9534.JPG', alt: 'Détail des perles et médailles du collier Éclat de tradition' }, { src: '/assets/product1/gemini-2.5-flash-image_ne_change_pas_le_collier_mis_le_dans_un_buste_porte-colliere_Le_porte-collier_es-0 (2).jpg', alt: 'Collier Éclat de tradition porté sur un buste clair' }] },
  { id: '6', slug: 'medailles-du-sud', reference: 'TIF–02', name: 'Médailles du Sud', description: 'Une composition généreuse de perles corail et de médaillons, pensée pour donner du caractère à une silhouette.', image: '/assets/product2/product2.jpg', alt: 'Collier composé de perles corail et de médaillons', priceCents: 1990, images: [{ src: '/assets/product2/product2.jpg', alt: 'Collier Médailles du Sud sur fond noir' }, { src: '/assets/product2/IMG_9537.JPG', alt: 'Vue du collier Médailles du Sud' }, { src: '/assets/product2/IMG_9539.JPG', alt: 'Détail des perles et médaillons du collier Médailles du Sud' }] },
  { id: '5', slug: 'spirales-d-argent', reference: 'TIF–03', name: 'Spirales d’héritage', description: 'Perles ambrées et pendentifs spiralés forment un bijou à la fois graphique et délicat.', image: '/assets/product3/product3.jpg', alt: 'Collier aux perles ambrées et pendentifs en spirale', priceCents: 1990, images: [{ src: '/assets/product3/product3.jpg', alt: 'Collier Spirales d’argent présenté sur un buste' }, { src: '/assets/product3/IMG_9541.JPG', alt: 'Vue d’ensemble du collier Spirales d’argent' }, { src: '/assets/product3/IMG_9542.JPG', alt: 'Détail des pendentifs spiralés du collier Spirales d’argent' }] },
  { id: '3', slug: 'lien-d-heritage', reference: 'TIF–04', name: 'Lien d’héritage', description: 'Un collier lumineux ponctué de pièces argentées traditionnelles, à offrir ou à garder près de soi.', image: '/assets/product4/product4.jpg', alt: 'Collier aux perles orange et pièces argentées', priceCents: 2490, images: [{ src: '/assets/product4/product4.jpg', alt: 'Collier Lien d’héritage porté sur un buste' }, { src: '/assets/product4/IMG_9546.JPG', alt: 'Vue d’ensemble du collier Lien d’héritage' }, { src: '/assets/product4/IMG_9547.JPG', alt: 'Détail des perles du collier Lien d’héritage' }, { src: '/assets/product4/IMG_9548.JPG', alt: 'Détail des pièces argentées du collier Lien d’héritage' }] },
  { id: '2', slug: 'parure-berbere', reference: 'TIF–05', name: 'Collier Louban', description: 'Ce collier Louban associe des perles aux teintes chaudes, des touches de vert et des pendentifs de couleur argentée aux détails finement décorés. Un bijou au caractère affirmé, qui met en valeur une tenue sobre et accompagne aussi bien le quotidien que les occasions.', shortDescription: 'Collier Louban aux teintes chaleureuses, rehaussé de touches vertes et de pendentifs argentés. Une touche traditionnelle pour sublimer votre style.', image: '/assets/product5/product5.jpg', alt: 'Collier Louban aux perles chaudes, touches de vert et pendentifs argentés', priceCents: 5990, images: [{ src: '/assets/product5/product5.jpg', alt: 'Collier Louban porté sur un buste noir' }, { src: '/assets/product5/IMG_9554.jpg', alt: 'Vue du collier Louban' }, { src: '/assets/product5/product5_2.jpg', alt: 'Détail des pendentifs argentés du collier Louban' }] },
  { id: '4', slug: 'presence-amazighe', reference: 'TIF–06', name: 'Tazrzit amazighe', description: 'Le caractère d’un bijou d’inspiration traditionnelle à porter au quotidien ou pour accompagner une tenue de fête.', image: '/assets/product6/product6.jpeg', alt: 'Collier présenté sur un buste', priceCents: 1990, images: [{ src: '/assets/product6/product6.jpeg', alt: 'Collier Présence amazighe sur un buste clair' }, { src: '/assets/product6/product6_1.jpeg', alt: 'Vue du collier Présence amazighe sur un buste noir' }, { src: '/assets/product6/product6_2.jpeg', alt: 'Détail du collier Présence amazighe' }] },
];

export const shippingCents = 0;
export const shippingDescription = 'Livraison offerte en France, une seule fois par commande.';
export const returnsDescription = 'Les conditions de retour ne sont pas encore définies dans le projet. Contactez-nous avant la commande pour les confirmer.';
export const getProduct = (slug: string) => products.find((product) => product.slug === slug);
