export class Message {
  id: number;
  title: string;
  summary: string;
  text: string;
  publishedAt: string;
  images?: { id: number }[];

}

export class MessageCreate {
  id: number;
  title: string;
  summary: string;
  text: string;
  publishedAt: string;
  images?: File[];

}
