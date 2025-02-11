'use client';
import { LoginMemberContext } from '@/stores/auth/LoginMember';
import { use, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';

interface GroupMembership {
  groupId: number;
  categoryName: string;
  name: string;
  modifiedAt: string;
  isApplying: boolean | null;
  isRejected: boolean | null;
  isMember: boolean | null;
  isAdmin: boolean | null;
}

export default function ClientPage() {
  // 날짜 포맷팅 함수
  const formatDate = (dateString: string) => {
    if (!dateString) return '정보 없음';
    return dateString.split('T')[0];
  };

  // 권한 표시 포맷팅
  // const formatAuthorities = (authorities: Array<{authority: string}>) => {
  //     return authorities.map(auth => {
  //         switch(auth.authority) {
  //             case 'ROLE_USER':
  //                 return '일반 회원';
  //             case 'ROLE_ADMIN':
  //                 return '관리자';
  //             default:
  //                 return auth.authority;
  //         }
  //     }).join(', ');
  // };

  const { loginMember } = use(LoginMemberContext);
  const [groups, setGroups] = useState<GroupMembership[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const router = useRouter();

  // 회원 탈퇴 처리 함수 추가
  const handleWithdrawal = async () => {
    if (!window.confirm('정말로 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
      return;
    }

    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch('http://localhost:8080/api/v1/members', {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error('회원 탈퇴 처리 중 오류가 발생했습니다.');
      }

      const data = await response.json();
      if (data.isSuccess) {
        // refreshToken 쿠키 삭제
        document.cookie = 'refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        localStorage.removeItem('accessToken');
        alert('회원 탈퇴가 완료되었습니다.');
        window.location.href = '/';
      } else {
        alert(data.message || '회원 탈퇴 처리 중 오류가 발생했습니다.');
      }
    } catch (err) {
      alert('회원 탈퇴 처리 중 오류가 발생했습니다.');
    }
  };

  useEffect(() => {
    const fetchMyGroups = async () => {
      try {
        const token = localStorage.getItem('accessToken');
        const response = await fetch('http://localhost:8080/api/v1/members/mygroups', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!response.ok) {
          throw new Error('그룹 정보를 불러오는데 실패했습니다.');
        }

        const data = await response.json();
        if (data.isSuccess) {
          setGroups(data.data);
        } else {
          setError(data.message);
        }
      } catch (err) {
        setError('그룹 정보를 불러오는 중 오류가 발생했습니다.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchMyGroups();
  }, []);
  return (
    <div className='container mx-auto p-4 max-w-2xl'>
      <div className='bg-white rounded-lg shadow-lg p-6 mb-6'>
        <div className='mb-6'>
          <h2 className='text-2xl font-bold'>회원 정보</h2>
        </div>
        <div className='space-y-4'>
          <div className='grid grid-cols-3 gap-4'>
            <div className='font-semibold'>닉네임</div>
            <div className='col-span-2'>{loginMember.nickname}</div>

            <div className='font-semibold'>회원 ID</div>
            <div className='col-span-2'>{loginMember.username}</div>

            <div className='font-semibold'>비밀번호</div>
            <div className='col-span-2'>{loginMember.password}</div>

            <div className='font-semibold'>가입일</div>
            <div className='col-span-2'>{formatDate(loginMember.createdAt)}</div>

            {/* <div className="font-semibold">회원 권한</div>
                        <div className="col-span-2">{formatAuthorities(loginMember.role.map(auth => ({authority: auth})))}</div> */}

            <div className='font-semibold'>가입 방식</div>
            <div className='col-span-2'>{loginMember.provider}</div>
          </div>
        </div>

        {/* 회원 탈퇴 버튼 추가 */}
        <div className='mt-8 pt-6 border-t'>
          <button
            onClick={handleWithdrawal}
            className='bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600 transition-colors'
          >
            회원 탈퇴
          </button>
          <p className='mt-2 text-sm text-gray-500'>회원 탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.</p>
        </div>
      </div>

      <div className='bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6'>
        <div className='mb-6'>
          <h2 className='text-2xl font-bold'>내 모임 목록</h2>
        </div>

        {isLoading ? (
          <div className='text-center py-4'>로딩 중...</div>
        ) : error ? (
          <div className='text-red-500 text-center py-4'>{error}</div>
        ) : groups.length === 0 ? (
          <div className='text-center py-4 text-gray-500'>아직 가입한 모임이 없습니다.</div>
        ) : (
          <div className='space-y-4'>
            {groups.map((group) => (
              <div
                key={group.groupId}
                onClick={() => router.push(`/groups/${group.groupId}`)}
                className='border rounded-lg p-4 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors cursor-pointer'
              >
                <div className='flex justify-between items-start mb-2'>
                  <div>
                    <h3 className='font-semibold text-lg'>{group.name}</h3>
                    <span className='text-sm bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-100 px-2 py-1 rounded-full'>
                      {group.categoryName}
                    </span>
                  </div>
                  <div className='flex gap-2'>
                    {group.isApplying && (
                      <span className='px-2 py-1 text-sm bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-100 rounded-full'>
                        신청 중
                      </span>
                    )}
                    {group.isRejected && (
                      <span className='px-2 py-1 text-sm bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-100 rounded-full'>
                        거절됨
                      </span>
                    )}
                    {group.isMember && (
                      <span
                        className={`px-2 py-1 text-sm rounded-full ${
                          group.isAdmin
                            ? 'bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-100'
                            : 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-100'
                        }`}
                      >
                        {group.isAdmin ? '관리자' : '멤버'}
                      </span>
                    )}
                  </div>
                </div>
                <div className='text-sm text-gray-500 dark:text-gray-400'>
                  최근 수정일: {new Date(group.modifiedAt).toLocaleDateString()}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
