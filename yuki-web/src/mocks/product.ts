import type { PageResult } from '@/types/api'
import type {
  AdminProductQuery,
  AdminProductVO,
  AdminReviewQuery,
  AdminReviewVO,
  CategorySaveDTO,
  ProductSaveDTO,
  SkuSaveDTO,
} from '@/types/admin'
import type {
  BrandVO,
  CategoryTreeVO,
  ProductDetailVO,
  ProductListVO,
  ProductQuery,
  ProductSort,
  ReviewQuery,
  ReviewVO,
  SkuVO,
} from '@/types/product'
import { formatCents, toCents } from '@/utils/money'
import { ApiError } from '@/utils/request'

// 数据照抄 docs/02_seed.sql，图片 URL 与后端种子数据完全一致；
// 后端阶段二完成、删掉 mock 分支后页面零改动

const img = (prompt: string) =>
  `https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=${prompt}&image_size=square`

interface SeedProduct extends Omit<ProductDetailVO, 'skus' | 'images' | 'reviewSummary'> {
  status: number // 0-下架 1-上架
}

const products: SeedProduct[] = [
  {
    id: 1, categoryId: 111, brandId: 1,
    name: 'Apple iPhone 15 Pro 钛金属设计',
    subtitle: 'A17 Pro 芯片 · 4800万像素主摄 · 全系USB-C',
    mainImage: img('black%20titanium%20smartphone%20product%20photo%20white%20background%20studio%20light'),
    detail: '<p>钛金属边框，A17 Pro 芯片，Pro 级摄像头系统，USB-C 接口。官方正品，全国联保。</p>',
    priceMin: '8999.00', priceMax: '10999.00', sales: 5230, status: 1,
  },
  {
    id: 2, categoryId: 111, brandId: 2,
    name: '华为 Mate 60 Pro',
    subtitle: '玄武架构 · 昆仑玻璃 · 卫星通话',
    mainImage: img('jade%20green%20flagship%20smartphone%20product%20photo%20white%20background%20studio%20light'),
    detail: '<p>玄武架构机身，第二代昆仑玻璃，支持卫星通话，HarmonyOS 4。官方正品，全国联保。</p>',
    priceMin: '6999.00', priceMax: '6999.00', sales: 8112, status: 1,
  },
  {
    id: 3, categoryId: 111, brandId: 3,
    name: '小米14',
    subtitle: '第三代骁龙8 · 徕卡光学 · 1.5K OLED 直屏',
    mainImage: img('compact%20white%20smartphone%20product%20photo%20white%20background%20studio%20light'),
    detail: '<p>第三代骁龙8 旗舰平台，徕卡专业光学镜头，90W 秒充。官方正品，全国联保。</p>',
    priceMin: '4299.00', priceMax: '4299.00', sales: 3560, status: 1,
  },
  {
    id: 4, categoryId: 121, brandId: 3,
    name: '小米手环8 Pro',
    subtitle: '1.74" 大屏 · 150+ 运动模式 · 14 天续航',
    mainImage: img('black%20fitness%20smart%20band%20wristband%20product%20photo%20white%20background'),
    detail: '<p>1.74 英寸大屏，支持 150+ 运动模式，14 天超长续航，5ATM 防水。</p>',
    priceMin: '399.00', priceMax: '399.00', sales: 12000, status: 1,
  },
  {
    id: 5, categoryId: 131, brandId: 6,
    name: '索尼 WH-1000XM5 头戴式无线降噪耳机',
    subtitle: '八麦克风降噪 · 30 小时续航 · LDAC',
    mainImage: img('black%20over-ear%20noise%20cancelling%20headphones%20product%20photo%20white%20background%20studio%20light'),
    detail: '<p>搭载双处理器与八麦克风系统，行业标杆级降噪，支持 LDAC 高解析音频。</p>',
    priceMin: '2399.00', priceMax: '2399.00', sales: 2100, status: 1,
  },
  {
    id: 6, categoryId: 211, brandId: 1,
    name: 'Apple MacBook Air 13 M3',
    subtitle: 'M3 芯片 · 18 小时续航 · 轻至 1.24kg',
    mainImage: img('slim%20midnight%20blue%20laptop%20product%20photo%20white%20background%20studio%20light'),
    detail: '<p>M3 芯片 8 核中央处理器，10 核图形处理器，最长 18 小时续航，Liquid 视网膜显示屏。</p>',
    priceMin: '8999.00', priceMax: '11499.00', sales: 1880, status: 1,
  },
  {
    id: 7, categoryId: 212, brandId: 4,
    name: '联想拯救者 Y9000P 2024',
    subtitle: 'i9-14900HX · RTX 4060/4080 · 2.5K 240Hz 电竞屏',
    mainImage: img('black%20gaming%20laptop%20rgb%20keyboard%20product%20photo%20white%20background%20studio%20light'),
    detail: '<p>14 代酷睿 i9 处理器，满血显卡功耗释放，霜刃 Pro 散热系统，2.5K 240Hz 电竞屏。</p>',
    priceMin: '9999.00', priceMax: '13999.00', sales: 990, status: 1,
  },
  {
    id: 8, categoryId: 311, brandId: 5,
    name: '美的 1.5匹 新一级能效 壁挂式空调',
    subtitle: '变频冷暖 · 智能温控 · 静音运行',
    mainImage: img('white%20wall%20mounted%20air%20conditioner%20indoor%20unit%20product%20photo%20light%20background'),
    detail: '<p>新一级能效变频压缩机，支持智能温控与自清洁，最低 20dB 静音运行。</p>',
    priceMin: '2299.00', priceMax: '2699.00', sales: 6700, status: 1,
  },
  {
    id: 9, categoryId: 411, brandId: 5,
    name: '美的 无线洗地机 S6',
    subtitle: '吸拖洗三合一 · 0缠毛滚刷 · 自清洁基站',
    mainImage: img('white%20wet%20dry%20vacuum%20cleaner%20product%20photo%20white%20background%20studio%20light'),
    detail: '<p>吸拖洗三合一，支持活水清洁与自清洁基站，适合家庭地面日常深度清洁。</p>',
    priceMin: '1999.00', priceMax: '1999.00', sales: 0, status: 0, // 下架，列表不可见
  },
]

const skus: Record<number, ProductDetailVO['skus']> = {
  1: [
    { id: 1, skuCode: 'P1-01', specs: '{"颜色":"钛黑色","存储":"256GB"}', price: '8999.00', stock: 120, image: img('black%20titanium%20smartphone%20product%20photo%20white%20background%20studio%20light') },
    { id: 2, skuCode: 'P1-02', specs: '{"颜色":"钛黑色","存储":"512GB"}', price: '10999.00', stock: 80, image: img('black%20titanium%20smartphone%20product%20photo%20white%20background%20studio%20light') },
    { id: 3, skuCode: 'P1-03', specs: '{"颜色":"原色钛","存储":"256GB"}', price: '8999.00', stock: 95, image: img('black%20titanium%20smartphone%20product%20photo%20white%20background%20studio%20light') },
  ],
  2: [
    { id: 4, skuCode: 'P2-01', specs: '{"颜色":"雅川青","存储":"512GB"}', price: '6999.00', stock: 200, image: img('jade%20green%20flagship%20smartphone%20product%20photo%20white%20background%20studio%20light') },
    { id: 5, skuCode: 'P2-02', specs: '{"颜色":"白沙银","存储":"512GB"}', price: '6999.00', stock: 150, image: img('jade%20green%20flagship%20smartphone%20product%20photo%20white%20background%20studio%20light') },
  ],
  3: [
    { id: 6, skuCode: 'P3-01', specs: '{"颜色":"黑色","存储":"16GB+512GB"}', price: '4299.00', stock: 300, image: img('compact%20white%20smartphone%20product%20photo%20white%20background%20studio%20light') },
    { id: 7, skuCode: 'P3-02', specs: '{"颜色":"白色","存储":"16GB+512GB"}', price: '4299.00', stock: 260, image: img('compact%20white%20smartphone%20product%20photo%20white%20background%20studio%20light') },
  ],
  4: [
    { id: 8, skuCode: 'P4-01', specs: '{"颜色":"黑色"}', price: '399.00', stock: 500, image: img('black%20fitness%20smart%20band%20wristband%20product%20photo%20white%20background') },
    { id: 9, skuCode: 'P4-02', specs: '{"颜色":"银色"}', price: '399.00', stock: 420, image: img('black%20fitness%20smart%20band%20wristband%20product%20photo%20white%20background') },
  ],
  5: [
    { id: 10, skuCode: 'P5-01', specs: '{"颜色":"黑色"}', price: '2399.00', stock: 180, image: img('black%20over-ear%20noise%20cancelling%20headphones%20product%20photo%20white%20background%20studio%20light') },
    { id: 11, skuCode: 'P5-02', specs: '{"颜色":"铂金银"}', price: '2399.00', stock: 140, image: img('black%20over-ear%20noise%20cancelling%20headphones%20product%20photo%20white%20background%20studio%20light') },
  ],
  6: [
    { id: 12, skuCode: 'P6-01', specs: '{"颜色":"午夜色","配置":"8GB+256GB"}', price: '8999.00', stock: 90, image: img('slim%20midnight%20blue%20laptop%20product%20photo%20white%20background%20studio%20light') },
    { id: 13, skuCode: 'P6-02', specs: '{"颜色":"星光色","配置":"8GB+256GB"}', price: '8999.00', stock: 70, image: img('slim%20midnight%20blue%20laptop%20product%20photo%20white%20background%20studio%20light') },
    { id: 14, skuCode: 'P6-03', specs: '{"颜色":"午夜色","配置":"16GB+512GB"}', price: '11499.00', stock: 45, image: img('slim%20midnight%20blue%20laptop%20product%20photo%20white%20background%20studio%20light') },
  ],
  7: [
    { id: 15, skuCode: 'P7-01', specs: '{"处理器":"i9-14900HX","显卡":"RTX 4060","内存硬盘":"16GB+1TB"}', price: '9999.00', stock: 60, image: img('black%20gaming%20laptop%20rgb%20keyboard%20product%20photo%20white%20background%20studio%20light') },
    { id: 16, skuCode: 'P7-02', specs: '{"处理器":"i9-14900HX","显卡":"RTX 4080","内存硬盘":"32GB+1TB"}', price: '13999.00', stock: 25, image: img('black%20gaming%20laptop%20rgb%20keyboard%20product%20photo%20white%20background%20studio%20light') },
  ],
  8: [
    { id: 17, skuCode: 'P8-01', specs: '{"匹数":"大1匹","能效":"新一级"}', price: '2299.00', stock: 350, image: img('white%20wall%20mounted%20air%20conditioner%20indoor%20unit%20product%20photo%20light%20background') },
    { id: 18, skuCode: 'P8-02', specs: '{"匹数":"1.5匹","能效":"新一级"}', price: '2699.00', stock: 280, image: img('white%20wall%20mounted%20air%20conditioner%20indoor%20unit%20product%20photo%20light%20background') },
  ],
  9: [
    { id: 19, skuCode: 'P9-01', specs: '{"颜色":"白色"}', price: '1999.00', stock: 1, image: img('white%20wet%20dry%20vacuum%20cleaner%20product%20photo%20white%20background%20studio%20light') },
  ],
}

const images: Record<number, string[]> = {
  1: [
    img('black%20titanium%20smartphone%20product%20photo%20white%20background%20studio%20light'),
    img('titanium%20smartphone%20camera%20closeup%20product%20photo%20white%20background'),
  ],
  2: [
    img('jade%20green%20flagship%20smartphone%20product%20photo%20white%20background%20studio%20light'),
    img('green%20smartphone%20circular%20camera%20back%20view%20product%20photo'),
  ],
  3: [
    img('compact%20white%20smartphone%20product%20photo%20white%20background%20studio%20light'),
    img('white%20smartphone%20side%20profile%20product%20photo%20white%20background'),
  ],
  4: [
    img('black%20fitness%20smart%20band%20wristband%20product%20photo%20white%20background'),
    img('smart%20band%20screen%20closeup%20product%20photo%20white%20background'),
  ],
  5: [
    img('black%20over-ear%20noise%20cancelling%20headphones%20product%20photo%20white%20background%20studio%20light'),
    img('black%20headphones%20folded%20side%20view%20product%20photo'),
  ],
  6: [
    img('slim%20midnight%20blue%20laptop%20product%20photo%20white%20background%20studio%20light'),
    img('slim%20laptop%20open%20side%20angle%20product%20photo%20white%20background'),
  ],
  7: [
    img('black%20gaming%20laptop%20rgb%20keyboard%20product%20photo%20white%20background%20studio%20light'),
    img('gaming%20laptop%20back%20view%20product%20photo%20white%20background'),
  ],
  8: [
    img('white%20wall%20mounted%20air%20conditioner%20indoor%20unit%20product%20photo%20light%20background'),
    img('white%20split%20air%20conditioner%20side%20view%20product%20photo'),
  ],
  9: [
    img('white%20wet%20dry%20vacuum%20cleaner%20product%20photo%20white%20background%20studio%20light'),
    img('wet%20dry%20vacuum%20cleaner%20charging%20dock%20product%20photo%20white%20background'),
  ],
}

const categories: CategoryTreeVO[] = [
  {
    id: 1, parentId: 0, name: '手机数码', level: 1, sort: 1,
    children: [
      { id: 11, parentId: 1, name: '手机通讯', level: 2, sort: 1, children: [{ id: 111, parentId: 11, name: '智能手机', level: 3, sort: 1 }] },
      { id: 12, parentId: 1, name: '智能穿戴', level: 2, sort: 2, children: [{ id: 121, parentId: 12, name: '智能手环', level: 3, sort: 1 }] },
      { id: 13, parentId: 1, name: '数码配件', level: 2, sort: 3, children: [{ id: 131, parentId: 13, name: '降噪耳机', level: 3, sort: 1 }] },
    ],
  },
  {
    id: 2, parentId: 0, name: '电脑办公', level: 1, sort: 2,
    children: [
      {
        id: 21, parentId: 2, name: '笔记本电脑', level: 2, sort: 1,
        children: [
          { id: 211, parentId: 21, name: '轻薄本', level: 3, sort: 1 },
          { id: 212, parentId: 21, name: '游戏本', level: 3, sort: 2 },
        ],
      },
    ],
  },
  {
    id: 3, parentId: 0, name: '家用电器', level: 1, sort: 3,
    children: [
      { id: 31, parentId: 3, name: '大家电', level: 2, sort: 1, children: [{ id: 311, parentId: 31, name: '空调', level: 3, sort: 1 }] },
    ],
  },
  {
    id: 4, parentId: 0, name: '家居生活', level: 1, sort: 4,
    children: [
      { id: 41, parentId: 4, name: '清洁电器', level: 2, sort: 1, children: [{ id: 411, parentId: 41, name: '洗地机', level: 3, sort: 1 }] },
    ],
  },
]

const brands: BrandVO[] = [
  { id: 1, name: 'Apple', description: '消费电子与软件生态' },
  { id: 2, name: '华为', description: '通信设备与智能终端' },
  { id: 3, name: '小米', description: '智能硬件与消费电子' },
  { id: 4, name: '联想', description: '全球个人计算设备厂商' },
  { id: 5, name: '美的', description: '白色家电制造商' },
  { id: 6, name: '索尼', description: '影音娱乐设备厂商' },
]

// 种子只有 1 条评价；为演示星级筛选与分页，补充若干条（mock 是对后端输出的"预测"）
const reviews: ReviewVO[] = [
  { id: 1, userId: 1, nickname: '由岐', productId: 1, skuId: 1, rating: 5, content: '钛金属质感拉满，A17 Pro 性能强劲，拍照素质一流，发货也快，五星好评！', isAnonymous: 0, status: 1, createdAt: '2026-09-03 18:25:00' },
  { id: 2, userId: 2, nickname: 'Tom', productId: 1, skuId: 2, rating: 4, content: '整体满意，续航比上一代好，就是价格有点硬。', isAnonymous: 0, status: 1, createdAt: '2026-09-04 09:12:00' },
  { id: 3, userId: 1, nickname: '匿**', productId: 1, skuId: 3, rating: 5, content: '原色钛很耐看，USB-C 终于统一了充电线。', isAnonymous: 1, status: 1, createdAt: '2026-09-05 14:40:00' },
  { id: 4, userId: 2, nickname: 'Tom', productId: 2, skuId: 4, rating: 5, content: '雅川青配色绝了，卫星通话在郊区实测可用。', isAnonymous: 0, status: 1, createdAt: '2026-09-05 20:03:00' },
  { id: 5, userId: 1, nickname: '由岐', productId: 3, skuId: 6, rating: 4, content: '徕卡色彩讨喜，直屏手感好，充电是真的快。', isAnonymous: 0, status: 1, createdAt: '2026-09-06 11:26:00' },
  { id: 6, userId: 2, nickname: 'Tom', productId: 4, skuId: 8, rating: 3, content: '功能够用，屏幕亮度户外一般，续航符合宣传。', isAnonymous: 0, status: 1, createdAt: '2026-09-06 16:52:00' },
  { id: 7, userId: 1, nickname: '由岐', productId: 5, skuId: 10, rating: 5, content: '降噪确实是第一梯队，戴一整天也不夹头。', isAnonymous: 0, status: 1, createdAt: '2026-09-07 08:31:00' },
  { id: 8, userId: 2, nickname: 'Tom', productId: 7, skuId: 15, rating: 2, content: '性能没问题，但高负载风扇噪音偏大，介意慎拍。', isAnonymous: 0, status: 1, createdAt: '2026-09-07 19:45:00' },
]

// 库存是可变状态：下单扣减、取消恢复，模拟后端条件更新
const stockMap = new Map<number, number>()
Object.values(skus)
  .flat()
  .forEach((s) => stockMap.set(s.id, s.stock))

/** SKU 规格 JSON → 快照文本 "颜色:黑;存储:256G"（与订单/购物车快照格式一致） */
function specsSnapshot(specsJson: string): string {
  try {
    return Object.entries(JSON.parse(specsJson) as Record<string, string>)
      .map(([k, v]) => `${k}:${v}`)
      .join(';')
  } catch {
    return specsJson
  }
}

// ---- 查询逻辑：模拟后端规范（上架过滤、筛选、排序、分页） ----

function pageOf<T>(list: T[], page = 1, pageSize = 20): PageResult<T> {
  const start = (page - 1) * pageSize
  return { list: list.slice(start, start + pageSize), page, pageSize, total: list.length }
}

/** 选中某分类时，其所有后代三级分类下的商品都算命中 */
function categoryIdsWithDescendants(id: number): Set<number> {
  const result = new Set<number>([id])
  const walk = (nodes: CategoryTreeVO[]) => {
    for (const n of nodes) {
      if (result.has(n.id)) n.children?.forEach((c) => result.add(c.id))
      if (n.children) walk(n.children)
    }
  }
  walk(categories)
  return result
}

function toListVO(p: SeedProduct): ProductListVO {
  return {
    id: p.id, name: p.name, subtitle: p.subtitle, mainImage: p.mainImage,
    priceMin: p.priceMin, priceMax: p.priceMax, sales: p.sales,
  }
}

// ---- 管理端辅助 ----

function toAdminVO(p: SeedProduct): AdminProductVO {
  return {
    id: p.id, categoryId: p.categoryId, brandId: p.brandId,
    name: p.name, subtitle: p.subtitle, mainImage: p.mainImage,
    priceMin: p.priceMin, priceMax: p.priceMax, sales: p.sales, status: p.status,
  }
}

/** SKU 增删改后重算 SPU 价格区间（与后端一致：取启用 SKU 的 min/max） */
function recalcPriceRange(p: SeedProduct) {
  const list = skus[p.id] ?? []
  if (!list.length) {
    p.priceMin = '0.00'
    p.priceMax = '0.00'
    return
  }
  const cents = list.map((s) => toCents(s.price))
  p.priceMin = formatCents(Math.min(...cents))
  p.priceMax = formatCents(Math.max(...cents))
}

function findCategoryNode(id: number): CategoryTreeVO | null {
  let found: CategoryTreeVO | null = null
  const walk = (nodes: CategoryTreeVO[]) => {
    for (const n of nodes) {
      if (found) return
      if (n.id === id) {
        found = n
        return
      }
      if (n.children) walk(n.children)
    }
  }
  walk(categories)
  return found
}

function nextCategoryId(): number {
  let max = 0
  const walk = (nodes: CategoryTreeVO[]) => {
    for (const n of nodes) {
      max = Math.max(max, n.id)
      if (n.children) walk(n.children)
    }
  }
  walk(categories)
  return max + 1
}

const sorters: Record<Exclude<ProductSort, 'default'>, (a: SeedProduct, b: SeedProduct) => number> = {
  price_asc: (a, b) => toCents(a.priceMin) - toCents(b.priceMin),
  price_desc: (a, b) => toCents(b.priceMin) - toCents(a.priceMin),
  sales: (a, b) => b.sales - a.sales,
  newest: (a, b) => b.id - a.id, // 用 id 近似创建时间
}

export const productFixtures = {
  tree(): CategoryTreeVO[] {
    return categories
  },

  brands(page = 1, pageSize = 20): PageResult<BrandVO> {
    return pageOf(brands, page, pageSize)
  },

  page(q: ProductQuery): PageResult<ProductListVO> {
    let list = products.filter((p) => p.status === 1)

    if (q.keyword) {
      const kw = q.keyword.toLowerCase()
      list = list.filter(
        (p) => p.name.toLowerCase().includes(kw) || p.subtitle?.toLowerCase().includes(kw),
      )
    }
    if (q.categoryId) {
      const ids = categoryIdsWithDescendants(q.categoryId)
      list = list.filter((p) => ids.has(p.categoryId))
    }
    if (q.brandId) list = list.filter((p) => p.brandId === q.brandId)
    if (q.minPrice) list = list.filter((p) => toCents(p.priceMax) >= toCents(q.minPrice!))
    if (q.maxPrice) list = list.filter((p) => toCents(p.priceMin) <= toCents(q.maxPrice!))
    if (q.sort && q.sort !== 'default') list = [...list].sort(sorters[q.sort])

    return pageOf(list.map(toListVO), q.page, q.pageSize)
  },

  detail(id: number): ProductDetailVO | null {
    const p = products.find((p) => p.id === id && p.status === 1)
    if (!p) return null
    const productReviews = reviews.filter((r) => r.productId === id && r.status === 1)
    const avg = productReviews.length
      ? (productReviews.reduce((s, r) => s + r.rating, 0) / productReviews.length).toFixed(1)
      : undefined
    const { status: _, ...spu } = p
    return {
      ...spu,
      skus: (skus[id] ?? []).map((s) => ({ ...s, stock: stockMap.get(s.id) ?? s.stock })),
      images: images[id] ?? [],
      reviewSummary: avg ? { ratingAvg: avg, reviewCount: productReviews.length } : undefined,
    }
  },

  reviews(productId: number, q: ReviewQuery): PageResult<ReviewVO> {
    let list = reviews.filter((r) => r.productId === productId && r.status === 1)
    if (q.rating) list = list.filter((r) => r.rating === q.rating)
    return pageOf(list, q.page, q.pageSize)
  },

  // ---- 供 cart/order mock 使用的 SKU 查询与库存操作 ----

  skuInfo(skuId: number) {
    for (const p of products) {
      const sku = (skus[p.id] ?? []).find((s) => s.id === skuId)
      if (sku) {
        return {
          skuId,
          productId: p.id,
          productName: p.name,
          skuSpecs: specsSnapshot(sku.specs),
          image: sku.image ?? p.mainImage,
          price: sku.price,
          stock: stockMap.get(skuId) ?? 0,
          onSale: p.status === 1,
        }
      }
    }
    return null
  },

  /** 条件扣减：库存不足返回 false，模拟后端 stock >= quantity 条件更新 */
  deductStock(skuId: number, quantity: number): boolean {
    const cur = stockMap.get(skuId) ?? 0
    if (cur < quantity) return false
    stockMap.set(skuId, cur - quantity)
    return true
  },

  restoreStock(skuId: number, quantity: number) {
    stockMap.set(skuId, (stockMap.get(skuId) ?? 0) + quantity)
  },

  /** 提交评价后追加到商品评价列表，详情页摘要随之更新 */
  addReview(r: Omit<ReviewVO, 'id' | 'status' | 'createdAt'>): ReviewVO {
    const review: ReviewVO = {
      ...r,
      id: Math.max(0, ...reviews.map((x) => x.id)) + 1,
      status: 1,
      createdAt: new Date().toLocaleString('zh-CN', { hour12: false }).replaceAll('/', '-'),
    }
    reviews.unshift(review)
    return review
  },

  // ---- 管理端操作（与用户端同一份数据，模拟后端同表读写） ----

  /** 管理端商品详情：不过滤下架（公开 detail 只返回上架商品） */
  adminDetail(id: number): ProductDetailVO {
    const p = products.find((p) => p.id === id)
    if (!p) throw new ApiError(40400, '商品不存在')
    const { status: _, ...spu } = p
    return {
      ...spu,
      skus: (skus[id] ?? []).map((s) => ({ ...s, stock: stockMap.get(s.id) ?? s.stock })),
      images: images[id] ?? [],
    }
  },

  /** 商品分页：不过滤下架，支持关键词与状态筛选 */
  adminPage(q: AdminProductQuery): PageResult<AdminProductVO> {
    let list = [...products]
    if (q.keyword) {
      const kw = q.keyword.toLowerCase()
      list = list.filter((p) => p.name.toLowerCase().includes(kw))
    }
    if (q.status !== undefined) list = list.filter((p) => p.status === q.status)
    list.sort((a, b) => b.id - a.id)
    return pageOf(list.map(toAdminVO), q.page, q.pageSize)
  },

  /** 新建 SPU：默认上架、无 SKU，价格区间由后续 SKU 重算 */
  createProduct(dto: ProductSaveDTO): AdminProductVO {
    const id = Math.max(0, ...products.map((p) => p.id)) + 1
    const p: SeedProduct = {
      id,
      categoryId: dto.categoryId,
      brandId: dto.brandId,
      name: dto.name,
      subtitle: dto.subtitle,
      mainImage: dto.mainImage,
      detail: dto.detail,
      priceMin: '0.00',
      priceMax: '0.00',
      sales: 0,
      status: 1,
    }
    products.unshift(p)
    skus[id] = []
    images[id] = dto.mainImage ? [dto.mainImage] : []
    return toAdminVO(p)
  },

  updateProduct(id: number, dto: ProductSaveDTO): AdminProductVO {
    const p = products.find((p) => p.id === id)
    if (!p) throw new ApiError(40400, '商品不存在')
    p.categoryId = dto.categoryId
    p.brandId = dto.brandId
    p.name = dto.name
    p.subtitle = dto.subtitle
    p.mainImage = dto.mainImage
    p.detail = dto.detail
    return toAdminVO(p)
  },

  setProductStatus(id: number, status: number) {
    const p = products.find((p) => p.id === id)
    if (!p) throw new ApiError(40400, '商品不存在')
    p.status = status
  },

  /** 新增 SKU：写可变库存，重算 SPU 价格区间 */
  addSku(productId: number, dto: SkuSaveDTO): SkuVO {
    const p = products.find((p) => p.id === productId)
    if (!p) throw new ApiError(40400, '商品不存在')
    const id = Math.max(0, ...Object.values(skus).flat().map((s) => s.id)) + 1
    const sku: SkuVO = {
      id,
      skuCode: dto.skuCode ?? `P${productId}-${String(id).padStart(2, '0')}`,
      specs: dto.specs,
      price: dto.price,
      stock: dto.stock,
      image: dto.image,
    }
    ;(skus[productId] ??= []).push(sku)
    stockMap.set(id, dto.stock)
    recalcPriceRange(p)
    return { ...sku }
  },

  /** 修改 SKU 价格/库存/规格；库存直接改写可变库存（与后台调库存语义一致） */
  updateSku(skuId: number, dto: Partial<SkuSaveDTO>): SkuVO {
    for (const p of products) {
      const sku = (skus[p.id] ?? []).find((s) => s.id === skuId)
      if (sku) {
        if (dto.skuCode !== undefined) sku.skuCode = dto.skuCode
        if (dto.specs !== undefined) sku.specs = dto.specs
        if (dto.price !== undefined) sku.price = dto.price
        if (dto.image !== undefined) sku.image = dto.image
        if (dto.stock !== undefined) {
          sku.stock = dto.stock
          stockMap.set(skuId, dto.stock)
        }
        recalcPriceRange(p)
        return { ...sku }
      }
    }
    throw new ApiError(40400, 'SKU 不存在')
  },

  /** 评价审核列表：含已隐藏，关联商品名 */
  adminReviews(q: AdminReviewQuery): PageResult<AdminReviewVO> {
    let list = reviews.map((r) => ({
      id: r.id,
      productId: r.productId,
      productName: products.find((p) => p.id === r.productId)?.name ?? `商品#${r.productId}`,
      nickname: r.nickname,
      rating: r.rating,
      content: r.content,
      isAnonymous: r.isAnonymous,
      status: r.status,
      createdAt: r.createdAt,
    }))
    if (q.status !== undefined) list = list.filter((r) => r.status === q.status)
    return pageOf(list, q.page, q.pageSize)
  },

  setReviewStatus(id: number, status: number) {
    const r = reviews.find((r) => r.id === id)
    if (!r) throw new ApiError(40400, '评价不存在')
    r.status = status
  },

  /** 分类管理：返回完整树（含停用位，mock 未建模 status 字段） */
  adminCategoryTree(): CategoryTreeVO[] {
    return categories
  },

  /** 新增（无 id）或编辑（有 id）分类；最多三级 */
  saveCategory(dto: CategorySaveDTO): CategoryTreeVO {
    if (dto.id != null) {
      const node = findCategoryNode(dto.id)
      if (!node) throw new ApiError(40400, '分类不存在')
      node.name = dto.name
      node.sort = dto.sort
      return node
    }
    const id = nextCategoryId()
    if (dto.parentId === 0) {
      const node: CategoryTreeVO = { id, parentId: 0, name: dto.name, level: 1, sort: dto.sort, children: [] }
      categories.push(node)
      return node
    }
    const parent = findCategoryNode(dto.parentId)
    if (!parent) throw new ApiError(40400, '父分类不存在')
    if (parent.level >= 3) throw new ApiError(40000, '最多支持三级分类')
    const node: CategoryTreeVO = { id, parentId: parent.id, name: dto.name, level: parent.level + 1, sort: dto.sort }
    ;(parent.children ??= []).push(node)
    return node
  },
}
