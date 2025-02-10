import api from "@/api";

export const getComments = async (
  postId: number,
  page: number,
  token: string
) => {
  try {
    const response = await api.get(`/comment/${postId}`, {
      headers: { Authorization: `Bearer ${token}` },
      params: { page },
    });
    return response.data.data;
  } catch (error: any) {
    throw new Error("댓글 목록을 불러오는데 실패했습니다");
  }
};

export const createComment = async (
  postId: number,
  content: string,
  token: string
): Promise<any> => {
  try {
    const payload = { content };
    const response = await api.post(`/comment/${postId}`, payload, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  } catch (error: any) {
    throw new Error("댓글 작성에 실패했습니다.");
  }
};

export const updateComment = async (
  commentId: number,
  content: string,
  token: string
): Promise<any> => {
  try {
    const payload = { content };
    const response = await api.patch(`/comment/${commentId}`, payload, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data.data;
  } catch (error: any) {
    throw new Error("댓글 수정에 실패했습니다.");
  }
};

export const deleteComment = async (
  commentId: number,
  token: string
): Promise<any> => {
  try {
    const response = await api.delete(`/comment/${commentId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data.data;
  } catch (error: any) {
    throw new Error("댓글 삭제에 실패했습니다.");
  }
};

export const getReplies = async (
  commentId: number,
  token: string
): Promise<any> => {
  try {
    const response = await api.get(`/comment/${commentId}/reply`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data.data;
  } catch (error: any) {
    throw new Error("답글을 불러오는데 실패했습니다.");
  }
};

export const createReply = async (
  commentId: number,
  replyContent: string,
  token: string
): Promise<any> => {
  try {
    const payload = { content: replyContent };
    const response = await api.post(`/comment/${commentId}/reply`, payload, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data.data;
  } catch (error: any) {
    throw new Error("답글 등록에 실패했습니다.");
  }
};

export const updateReply = async (
  replyId: number,
  replyContent: string,
  token: string
): Promise<any> => {
  try {
    const payload = { content: replyContent };
    const response = await api.patch(`/comment/${replyId}/reply`, payload, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data.data;
  } catch (error: any) {
    throw new Error("답글 수정에 실패했습니다.");
  }
};

export const deleteReply = async (
  replyId: number,
  token: string
): Promise<any> => {
  try {
    const response = await api.delete(`/comment/${replyId}/reply`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  } catch (error: any) {
    throw new Error("답글 삭제에 실패했습니다.");
  }
};
