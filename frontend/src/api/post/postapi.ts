import { Post } from "@/types/Post";
import { Comment } from "@/types/Comment";
import api from "@/api";

export const getPost = async (postId: number, token: string): Promise<Post> => {
  try {
    const response = await api.get(`/post/${postId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data.data;
  } catch (error: any) {
    throw error.response.data.code;
  }
};

export const getPosts = async (
  groupId: number,
  search: string,
  postStatus: string,
  page: number,
  token: string
) => {
  const queryParams = new URLSearchParams({
    groupId: groupId.toString(),
    search,
    postStatus: postStatus === "" ? "ALL" : postStatus,
    page: page.toString(),
  });

  try {
    const response = await api.get(`/post?${queryParams.toString()}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data.data;
  } catch (error: any) {
    throw new Error("게시물 목록을 불러오는데 실패했습니다");
  }
};

export const updatePost = async (
  postId: number,
  formData: FormData,
  token: string
) => {
  try {
    const response = await api.patch(`/post/${postId}`, formData, {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "multipart/form-data",
      },
    });

    return response.data;
  } catch (error: any) {
    throw error.response.data.code;
  }
};

export const deletePost = async (postId: number, token: string) => {
  try {
    await api.delete(`/post/${postId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
  } catch (error: any) {
    throw new Error("게시글을 삭제하는데 실패했습니다");
  }
};

export const downloadDoc = async (
  attachmentId: number,
  token: string | null
): Promise<Blob> => {
  try {
    const response = await api.get(`/download/post/${attachmentId}`, {
      headers: { Authorization: `Bearer ${token}` },
      responseType: "blob",
    });
    return response.data;
  } catch (error: any) {
    throw new Error("파일 다운로드에 실패했습니다");
  }
};
