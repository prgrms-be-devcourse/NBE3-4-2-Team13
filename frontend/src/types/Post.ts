export interface Post {
  postId: number;
  title: string;
  content: string;
  postStatus: "PUBLIC" | "PRIVATE" | "NOTICE";
  nickName: string;
  memberId: number;
  groupId: number;
  createdAt: string;
  modifiedAt: string;
  images: PostImage[];
  documents: PostDocument[];
}

export interface PostImage {
  attachmentId: number;
  fileName: string;
  fileType: string;
  filePath: string;
  fileSize: number;
}

export interface PostDocument {
  attachmentId: number;
  fileName: string;
  fileType: string;
  fileSize: number;
}
