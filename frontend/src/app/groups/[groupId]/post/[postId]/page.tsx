"use client";

import RequireAuthenticated from "@/lib/auth/components/RequireAuthenticated";
import PostContent from "./PostContent";
import CommentList from "./CommentList";
import { useRouter, useParams } from "next/navigation";
import { getPost } from "@/api/post/postapi";
import { useEffect, useRef, useState } from "react";
import { Post } from "@/types/Post";

function PostPage() {
  const params = useParams();
  const [post, setPost] = useState<Post | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();
  const postId = Number(params.postId);
  const groupId = Number(params.groupId);
  const hasFetched = useRef(false);

  useEffect(() => {
    const fetchPost = async () => {
      if (hasFetched.current) return;

      const token = localStorage.getItem("accessToken");

      if (!token) {
        router.push("/");
        return;
      }

      try {
        const data = await getPost(postId, token);
        setPost(data);
      } catch (error) {
        console.log(error);
        if (error && error == "GM001") {
          alert("접근권한이 없습니다");
        }else{error == "P001"}{
          alert("존재하지 않는 게시글입니다")
        }
        router.back();
      } finally {
        setLoading(false);
      }
    };
    fetchPost();

    hasFetched.current = true;
  }, [postId]);

  if (loading) return <p>로딩 중...</p>;
  if (!post) return;

  return (
    <div className="container mx-auto p-4">
      <RequireAuthenticated>
        <PostContent post={post} />
        <CommentList postId={postId} />
      </RequireAuthenticated>
    </div>
  );
}

export default PostPage;
