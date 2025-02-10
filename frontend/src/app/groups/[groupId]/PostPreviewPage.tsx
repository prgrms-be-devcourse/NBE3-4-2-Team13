'use client';

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getPosts } from "@/api/post/postapi";
import { Post } from "@/types/Post";

interface Props {
  groupId: string;
}

export default function PostPreviewPage({ groupId }: Props) {
  const router = useRouter();
  const [posts, setPosts] = useState<Post[]>([]);
  const token = localStorage.getItem("accessToken") || "";

  useEffect(() => {
    const fetchPosts = async () => {
      try {
        const data = await getPosts(Number(groupId), "", "ALL", 0, token);
        setPosts(data.content.slice(0, 3));
      } catch (error) {
        console.error(error);
      }
    };

    fetchPosts();
  }, [groupId, token]);

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-300 dark:border-gray-700 p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white">게시판</h2>
          <button
            onClick={() => router.push(`/groups/${groupId}/post`)}
            className="border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 px-4 py-2 rounded-lg text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
          >
            게시판 이동 →
          </button>
        </div>

        <div className="divide-y divide-gray-300 dark:divide-gray-700">
          {posts.length > 0 ? (
            posts.map((post, index) => (
              <div
                key={post.postId}
                className="p-4 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                onClick={() => router.push(`/groups/${groupId}/post/${post.postId}`)}
              >
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">{post.title}</h3>
                <p className="text-sm text-gray-600 dark:text-gray-400">
                  작성자: {post.nickName} · {new Date(post.createdAt).toLocaleDateString()}
                </p>
              </div>
            ))
          ) : (
            <p className="text-gray-500 dark:text-gray-400 text-center py-4">게시물이 없습니다.</p>
          )}
        </div>
      </div>
    </div>
  );
}
