package lp;

public class LP_Lib {
	
	private int n;   // 制約条件の数
	private int m;   // 変数の数
	private int m_s;   // スラック変数の数
	private int m_a;   // 人為変数の数
	private int mm;   // m + m_s + m_a
	private int a[];   // 人為変数があるか否か
	private int cp[];   // 比較演算子（-1:左辺<右辺, 0:左辺=右辺, 1:左辺>右辺）
	private int row[];   // 各行の基底変数の番号
	private double z[];   // 目的関数の係数
	private double s[][];   // 単体表
	private double eps;   // 許容誤差
	private int err;   // エラーコード （0:正常終了, 1:解無し）

	/******************************/
	/* コンストラクタ             */
	/*      n : 制約条件の数     */
	/*      m : 変数の数         */
	/*      z : 目的関数の係数   */
	/*      eq_l : 制約条件の左辺 */
	/*      eq_r : 制約条件の右辺 */
	/*      cp : 比較演算子      */
	/******************************/
	public LP_Lib(int n, int m, double z[], double eq_l[][], double eq_r[], int cp[])
	{
		this.n   = n;
		this.m   = m;
		this.z = z;
		//this.cp = cp;
		
		int i1, i2, k;
		// 初期設定
		eps = 1.0e-10;
		err = 0;
		
		a   = new int [n];
		for (int i = 0; i < this.n; i++) a[i]  = 0;
		
		// スラック変数と人為変数の数を数える
		m_s = 0;
		m_a = 0;
		for (int i = 0; i < n; i++) {
			if (cp[i] == 0) { //等式の場合は人為変数追加
				m_a++;
				if (eq_r[i] < 0.0) {
					eq_r[i] = -eq_r[i]; //右辺を移項している
					for (int j = 0; j < m; j++)
						eq_l[i][j] = -eq_l[i][j]; //左辺の符号を反対に
				}
			}
			else {//等式以外ではスラック変数を追加
				m_s++;
				if (eq_r[i] < 0.0) {
					cp[i]   = -cp[i];
					eq_r[i] = -eq_r[i];
					for (int j = 0; j < m; j++)
						eq_l[i][j] = -eq_l[i][j];
				}
				if (cp[i] > 0)
					m_a++;
			}
		}
		// 単体表の作成
		// 初期設定
		this.mm  = m + m_s + m_a;
		row = new int [n];
		s   = new double [n+1][mm+1];
		for (i1 = 0; i1 <= n; i1++) {
			if (i1 < n) {
				s[i1][0] = eq_r[i1];
				for (i2 = 0; i2 < m; i2++)
					s[i1][i2+1] = eq_l[i1][i2];
				for (i2 = m+1; i2 <= mm; i2++)
					s[i1][i2] = 0.0;
			}
			else {
				for (i2 = 0; i2 <= mm; i2++)
					s[i1][i2] = 0.0;
			}
		}
							// スラック変数
		k = m + 1;
		for (i1 = 0; i1 < n; i1++) {
			if (cp[i1] != 0) {
				if (cp[i1] < 0) {
					s[i1][k] = 1.0;
					row[i1]  = k - 1;
				}
				else
					s[i1][k] = -1.0;
				k++;
			}
		}
							// 人為変数
		for (i1 = 0; i1 < n; i1++) {
			if (cp[i1] >= 0) {
				s[i1][k] = 1.0;
				row[i1]  = k - 1;
				a[i1]    = 1;
				k++;
			}
		}
							// 目的関数
		if (m_a == 0) {
			for (i1 = 0; i1 < m; i1++)
				s[n][i1+1] = -z[i1];
		}
		else {
			for (i1 = 0; i1 <= m+m_s; i1++) {
				for (i2 = 0; i2 < n; i2++) {
					if (a[i2] > 0)
						s[n][i1] -= s[i2][i1];
				}
			}
		}
	}
	
	public int[] getRow() {
		return row;
	}

	public double[][] getS() {
		return s;
	}

	/*******************************/
	/* 最適化                      */
	/*      return : =0 : 正常終了 */
	/*             : =1 : 解無し   */
	/*******************************/
	int optimize()
	{
		int i1, i2, k;
		// フェーズ１
		opt_run();
		// フェーズ２
		if (err == 0 && m_a > 0) {
							// 目的関数の変更
			mm -= m_a;
			for (i1 = 0; i1 <= mm; i1++)
				s[n][i1] = 0.0;
			for (i1 = 0; i1 < n; i1++) {
				k = row[i1];
				if (k < m)
					s[n][0] += z[k] * s[i1][0];
			}
			for (i1 = 0; i1 < mm; i1++) {
				for (i2 = 0; i2 < n; i2++) {
					k = row[i2];
					if (k < m)
						s[n][i1+1] += z[k] * s[i2][i1+1];
				}
				if (i1 < m)
					s[n][i1+1] -= z[i1];
			}
							// 最適化
			opt_run();
		}

		return err;
	}

	/*******************************/
	/* 最適化（単体表の変形）      */
	/*******************************/
	void opt_run()
	{
		int i1, i2, p, q, k;
		double x, min;

		err = -1;
		while (err < 0) {
		
					// 列の選択（巡回を防ぐため必ずしも最小値を選択しない，Bland の規則）
			q = -1;
			for (i1 = 1; i1 <= mm && q < 0; i1++) {
				if (s[n][i1] < -eps)
					q = i1 - 1;
			}
					// 終了（最適解）
			if (q < 0)
				err = 0;
					// 行の選択（ Bland の規則を採用）
			else {
				p   = -1;
				k   = -1;
				min = 0.0;
				for (i1 = 0; i1 < n; i1++) {
					if (s[i1][q+1] > eps) {
						x = s[i1][0] / s[i1][q+1];
						if (p < 0 || x < min || x == min && row[i1] < k) {
							min = x;
							p   = i1;
							k   = row[i1];
						}
					}
				}
							// 解無し
				if (p < 0)
					err = 1;
							// 変形
				else {
					x      = s[p][q+1];
					row[p] = q;
					for (i1 = 0; i1 <= mm; i1++)
						s[p][i1] /= x;
					for (i1 = 0; i1 <= n; i1++) {
						if (i1 != p) {
							x = s[i1][q+1];
							for (i2 = 0; i2 <= mm; i2++)
								s[i1][i2] -= x * s[p][i2];
						}
					}
				}
			}
		}
	}

}
