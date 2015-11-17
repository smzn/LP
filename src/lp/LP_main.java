package lp;

import java.io.IOException;

public class LP_main {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		/*
		m : 変数の数、n : 式の数（制約条件の式の数）
		z : 目的関数の係数
		eq_l : 制約条件の左辺の係数
		eq_r : 制約条件の右辺の係数
		cp : 制約条件の符号の向き、<の時-1, >の時1, それ以外0
		
		[例題]
		目的関数，
		z = 3x1 + 2x2　　　(1)	
		を，制約条件，
			3x1 + x2 ≦ 9　　　(2)
			2.5x1 + 2x2 ≦ 12.5　　　(3)
			x1 + 2x2 ≦ 8　　　(4)
			x1， x2 ≧ 0
		のもとで，最大にする．
		x1 = 2，かつ，x2 = 3 のとき，最大値 12 
		*/
		/*DEA4件の場合
		int cp[] = {-1, -1, -1, -1, 0};
		double z[] = {0,0,410,9};
		double eq_l[][] = {{-24,-3530,410,9},{-7,-2330,284,6},{-77,-1910,450,24},{-7,-9710,175,8},{24,3530,0,0}};
		double eq_r[] = {0,0,0,0,1};
		*/
		/*
		//DEA7件の場合
		int cp[] = {-1, -1, -1, -1,-1, -1, -1, -1,-1,-1,-1,-1,-1,-1,-1,0};
		double z[] = {0,0,370,10};
		double eq_l[][] = {{-18,-900,370,10},{-74,-230,550,20},{-34,-2090,340,11},{-80,-1410,452,15},{-23,-370,350,18},{-3,-980,240,4},{-307,-1440,602,19},{-424,-400,936,24},{-11,-2370,98,6},{-30,-2560,312,11},{-57,-1750,500,18},{-17,-690,300,18},{-8,-3260,140,3},{-14,-12000,198,16},{-7,-2290,130,6},{18,900,0,0}};
		double eq_r[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1};
		*/
		
		int cp[] = {-1, -1, -1, -1,0};
		double z[] = {0,0,370,10};
		double eq_l[][] = {{-18,-900,370,10},{-74,-230,550,20},{-34,-2090,340,11},{-80,-1410,452,15},{18,900,0,0}};
		double eq_r[] = {0,0,0,0,1};
		
		// 実行
		LP_Lib lp = new LP_Lib(eq_r.length, z.length, z, eq_l, eq_r, cp);
		int sw = lp.optimize();
		
		// 結果の出力
		int row[] = lp.getRow();
		double s[][] = lp.getS();
		if (sw > 0)
			System.out.printf("\n解が存在しません\n");
			else {
				System.out.printf("\n(");
				for (int i = 0; i < z.length; i++) {
					double x = 0.0;
					for (int j = 0; j < eq_r.length; j++) {
						if (row[j] ==i) {
							x = s[j][0];
							break;
						}
					}
					if (i == 0)
						System.out.printf("%f", x);
					else
						System.out.printf(", %f", x);
				}
				System.out.printf(") のとき，最大値 %f\n", s[eq_r.length][0]);
			}
	
	

	}

}
