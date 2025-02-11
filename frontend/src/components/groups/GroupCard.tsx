interface GroupCardProps {
  group: {
    id: number;
    categoryName: string;
    name: string;
    province: string;
    city: string;
    town: string;
    recruitStatus: string;
    maxRecruitCount: number;
    currentMemberCount: number;
    createdAt: string;
    groupLeaders: string[];
  };
  onClick: () => void;
}

export default function GroupCard({ group, onClick }: GroupCardProps) {
  return (
    <div
      onClick={onClick}
      className='bg-white dark:bg-gray-800 rounded-lg shadow-md hover:shadow-lg transition-shadow duration-300 p-6 cursor-pointer'
    >
      <div className='flex justify-between items-center mb-4'>
        <span className='bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-100 px-3 py-1 rounded-full text-sm font-medium'>
          {group.categoryName}
        </span>
        <span className='text-gray-600 dark:text-gray-400 text-sm'>
          생성일: {new Date(group.createdAt).toLocaleDateString()}
        </span>
      </div>

      <h3 className='text-xl font-bold text-gray-900 dark:text-white mb-2'>{group.name}</h3>
      <div className='text-sm text-gray-500 dark:text-gray-400 mb-4'>관리자: {group.groupLeaders.join(', ')}</div>

      <div className='flex justify-between items-center'>
        <div className='text-gray-600 dark:text-gray-400'>
          <span>
            위치: {group.province} {group.city} {group.town}
          </span>
        </div>
        <div className='flex items-center space-x-4'>
          <span className='text-gray-600 dark:text-gray-400'>
            멤버: {group.currentMemberCount}/{group.maxRecruitCount}
          </span>
          <span
            className={`px-3 py-1 rounded-full text-sm font-medium ${
              group.recruitStatus === 'RECRUITING'
                ? 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-100'
                : 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-100'
            }`}
          >
            {group.recruitStatus === 'RECRUITING' ? '모집중' : '모집완료'}
          </span>
        </div>
      </div>
    </div>
  );
}
