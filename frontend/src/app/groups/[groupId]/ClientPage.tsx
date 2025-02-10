'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import KakaoMap from '@/components/groups/KakaoMap';
import { Button } from '@/components/ui/button';
import DeleteConfirmModal from '@/components/groups/DeleteConfirmModal';

interface GroupDetail {
  id: number;
  categoryName: string;
  name: string;
  province: string;
  city: string;
  town: string;
  description: string;
  recruitStatus: string;
  maxRecruitCount: number;
  currentMemberCount: number;
  createdAt: string;
  isMember: boolean;
  isAdmin: boolean;
}

interface Post {
  id: number;
  title: string;
  content: string;
  author: string;
  createdAt: string;
}

interface Props {
  groupId: string;
}

export default function ClientPage({ groupId }: Props) {
  const router = useRouter();
  const [group, setGroup] = useState<GroupDetail | null>(null);
  const [coordinates, setCoordinates] = useState<{ latitude: string; longitude: string } | null>(null);
  const [posts, setPosts] = useState<Post[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  console.log('GroupId received:', groupId);

  useEffect(() => {
    const fetchGroupData = async () => {
      try {
        const token = localStorage.getItem('accessToken');
        const response = await fetch(`http://localhost:8080/api/v1/groups/${groupId}`, {
          headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
          credentials: 'include',
        });
        const data = await response.json();
        if (data.isSuccess) {
          setGroup(data.data);
        }
      } catch (error) {
        console.error('Error fetching group:', error);
      }
    };

    if (groupId) {
      fetchGroupData();
    }
  }, [groupId]);

  useEffect(() => {
    if (group) {
      fetchCoordinates();
    }
  }, [group]);

  const fetchCoordinates = async () => {
    if (!group) return;

    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch(
        `http://localhost:8080/api/v1/proxy/kakao/address?province=${group.province}&city=${group.city}&town=${group.town}`,
        {
          headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
        }
      );
      const data = await response.json();
      console.log('Raw coordinates response:', data); // 디버깅용

      if (data.isSuccess && data.data.documents.length > 0) {
        const firstResult = data.data.documents[0];
        const coordinates = {
          latitude: firstResult.y,
          longitude: firstResult.x,
        };
        console.log('Setting coordinates:', coordinates); // 디버깅용
        setCoordinates(coordinates);
      }
    } catch (error) {
      console.error('Failed to fetch coordinates:', error);
    }
  };

  const handleLeaveGroup = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setError('로그인이 필요합니다.');
        return;
      }

      const response = await fetch(`http://localhost:8080/api/v1/groups/${groupId}/leave`, {
        method: 'DELETE',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('모임 탈퇴에 실패했습니다.');
      }

      router.push('/groups');
    } catch (error) {
      setError(error instanceof Error ? error.message : '모임 탈퇴 중 오류가 발생했습니다.');
    }
  };

  const handleJoinClick = () => {
    router.push(`/groups/${groupId}/join`);
  };

  const handleEditClick = () => {
    router.push(`/groups/${groupId}/edit`);
  };

  const handleDeleteClick = () => {
    setShowDeleteModal(true);
  };

  const handleDeleteConfirm = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setError('로그인이 필요합니다.');
        return;
      }

      const response = await fetch(`http://localhost:8080/api/v1/groups/${groupId}`, {
        method: 'DELETE',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('모임 삭제에 실패했습니다.');
      }

      router.push('/groups');
    } catch (error) {
      setError(error instanceof Error ? error.message : '모임 삭제 중 오류가 발생했습니다.');
    }
  };

  if (!group) {
    return <div>Loading...</div>;
  }

  return (
    <div className='container mx-auto px-4 py-8'>
      {/* 카카오맵 */}
      {coordinates && (
        <>
          {/* 디버깅용 좌표 출력 */}
          <div className='hidden'>Coordinates: {JSON.stringify(coordinates)}</div>
          <KakaoMap
            latitude={coordinates.latitude}
            longitude={coordinates.longitude}
            level={6}
            groupName={group.name}
            address={`${group.province} ${group.city} ${group.town}`}
          />
        </>
      )}

      {/* 그룹 상세 정보 */}
      <div className='bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6 mb-8'>
        <div className='flex justify-between items-center mb-6'>
          <div>
            <span className='bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-100 px-3 py-1 rounded-full text-sm font-medium'>
              {group.categoryName}
            </span>
            <h1 className='text-3xl font-bold text-gray-900 dark:text-white mt-2'>{group.name}</h1>
          </div>
          <div className='flex gap-2'>
            {group.isMember && (
              <button
                onClick={handleLeaveGroup}
                className='px-4 py-2 bg-amber-600 text-white rounded-md hover:bg-amber-700 transition-colors'
              >
                모임 탈퇴
              </button>
            )}

            {group.isAdmin && (
              <>
                <button
                  onClick={handleEditClick}
                  className='px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors'
                >
                  모임 수정
                </button>
                <button
                  onClick={handleDeleteClick}
                  className='px-4 py-2 bg-rose-600 text-white rounded-md hover:bg-rose-700 transition-colors'
                >
                  모임 삭제
                </button>
              </>
            )}

            {!group.isMember && (
              <button
                onClick={handleJoinClick}
                className='px-4 py-2 bg-emerald-600 text-white rounded-md hover:bg-emerald-700 transition-colors'
              >
                모임 가입
              </button>
            )}
          </div>
        </div>

        <div className='space-y-4 text-gray-600 dark:text-gray-300'>
          <p>
            <span className='font-semibold'>위치:</span> {group.province} {group.city} {group.town}
          </p>
          <p>
            <span className='font-semibold'>모집 상태:</span>{' '}
            <span
              className={`px-2 py-1 rounded-full text-sm ${
                group.recruitStatus === 'RECRUITING'
                  ? 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-100'
                  : 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-100'
              }`}
            >
              {group.recruitStatus === 'RECRUITING' ? '모집중' : '모집완료'}
            </span>
          </p>
          <p>
            <span className='font-semibold'>멤버:</span> {group.currentMemberCount}/{group.maxRecruitCount}
          </p>
          <p>
            <span className='font-semibold'>생성일:</span> {new Date(group.createdAt).toLocaleDateString()}
          </p>
          <div>
            <span className='font-semibold'>소개:</span>
            <p className='mt-2 whitespace-pre-wrap'>{group.description}</p>
          </div>
        </div>
      </div>

      {error && <div className='mt-4 p-4 bg-destructive/10 text-destructive rounded-md'>{error}</div>}

      {/* 게시판 섹션 (미구현) */}
      <div className='bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6'>
        <h2 className='text-2xl font-bold text-gray-900 dark:text-white mb-6'>게시판</h2>
        <div className='mb-4'>
          <form className='flex gap-4'>
            <input
              type='text'
              placeholder='게시글 검색'
              className='flex-1 px-4 py-2 border rounded-md dark:bg-gray-700 dark:border-gray-600 dark:text-white'
            />
            <button
              type='submit'
              className='px-6 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors'
            >
              검색
            </button>
          </form>
        </div>
        <div className='space-y-4'>
          {/* 게시글 목록 더미 데이터 */}
          <div className='p-4 border dark:border-gray-700 rounded-lg'>
            <h3 className='text-lg font-semibold text-gray-900 dark:text-white'>게시판 기능 준비중</h3>
            <p className='text-gray-600 dark:text-gray-400'>추후 업데이트 예정입니다.</p>
          </div>
        </div>
      </div>

      {/* 삭제 확인 모달 */}
      {showDeleteModal && (
        <DeleteConfirmModal onClose={() => setShowDeleteModal(false)} onConfirm={handleDeleteConfirm} />
      )}
    </div>
  );
}
