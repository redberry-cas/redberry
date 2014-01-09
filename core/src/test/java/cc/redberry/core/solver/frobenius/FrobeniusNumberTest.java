/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
 *   Stanislav Poslavsky   <stvlpos@mail.ru>
 *   Bolotin Dmitriy       <bolotin.dmitriy@gmail.com>
 *
 * This file is part of Redberry.
 *
 * Redberry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Redberry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Redberry. If not, see <http://www.gnu.org/licenses/>.
 */
package cc.redberry.core.solver.frobenius;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;

import static cc.redberry.core.solver.frobenius.FrobeniusNumber.frobeniusNumber;
import static java.math.BigInteger.valueOf;
import static org.junit.Assert.assertEquals;


/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class FrobeniusNumberTest {

    @Test
    public void test1() throws Exception {
        int[] A = {112, 432, 123, 7};
        assertEquals(frobeniusNumber(A), valueOf(731));
        A = new int[]{112, 432, 123, 7, 3};
        assertEquals(frobeniusNumber(A), valueOf(11));
        A = new int[]{112, 432, 122, 8, 31};
        assertEquals(frobeniusNumber(A), valueOf(147));
    }

    @Test
    public void testInfty() {
        int[] A = {112, 432, 122, 8, 32};
        assertEquals(frobeniusNumber(A), valueOf(-1));
    }

    @Ignore
    @Test(timeout = 10000)
    public void testMathematica() {
        assertEquals(frobeniusNumber(new int[]{605156, 628508, 491200, 61001, 338637, 384853, 776541, 722220, 831747, 146472}), valueOf(7019625));
        assertEquals(frobeniusNumber(new int[]{109126, 76512, 267533, 88380, 425857, 548061, 850815, 997038, 366551, 316272}), valueOf(4341499));
        assertEquals(frobeniusNumber(new int[]{213057, 228017, 231839, 621977, 688739, 150594, 699542, 766003, 8882, 114061}), valueOf(2906837));
        assertEquals(frobeniusNumber(new int[]{180941, 206179, 335850, 241557, 514571, 761137, 422401, 917399, 264083, 807119}), valueOf(6644336));
        assertEquals(frobeniusNumber(new int[]{80570, 748960, 698925, 858121, 755582, 540656, 756691, 609179, 383123, 920271}), valueOf(9573900));
        assertEquals(frobeniusNumber(new int[]{912453, 98556, 598466, 911012, 691779, 653854, 547189, 865622, 837834, 289999}), valueOf(10036578));
        assertEquals(frobeniusNumber(new int[]{102866, 928398, 306379, 119646, 101920, 894767, 718001, 392584, 597473, 497745}), valueOf(5276837));
        assertEquals(frobeniusNumber(new int[]{122307, 687661, 810932, 357614, 221343, 408796, 210059, 444375, 28439, 244445}), valueOf(3790970));
        assertEquals(frobeniusNumber(new int[]{883566, 51629, 73091, 560249, 883178, 492602, 358462, 718478, 882708, 717250}), valueOf(6373125));
        assertEquals(frobeniusNumber(new int[]{597221, 806006, 822316, 850636, 968093, 183689, 74750, 689751, 304023, 881691}), valueOf(8191457));
        assertEquals(frobeniusNumber(new int[]{196230, 872566, 831662, 512423, 284302, 741959, 755638, 186122, 830021, 875145}), valueOf(9690329));
        assertEquals(frobeniusNumber(new int[]{368505, 66328, 179374, 296224, 42136, 208857, 296522, 562771, 443646, 443518}), valueOf(3522207));
        assertEquals(frobeniusNumber(new int[]{43238, 500446, 669007, 575864, 729102, 233739, 303031, 496577, 645011, 618580}), valueOf(6325078));
        assertEquals(frobeniusNumber(new int[]{607533, 237596, 595437, 282917, 604507, 649689, 384188, 929783, 916663, 107578}), valueOf(7363394));
        assertEquals(frobeniusNumber(new int[]{724754, 963797, 902455, 536919, 496369, 398708, 880835, 387584, 570470, 603228}), valueOf(11348038));
        assertEquals(frobeniusNumber(new int[]{90486, 537239, 301423, 398633, 241448, 82187, 323025, 690855, 179430, 381046}), valueOf(4011838));
        assertEquals(frobeniusNumber(new int[]{958067, 448890, 840667, 930483, 490770, 741826, 407101, 12021, 442789, 278200}), valueOf(5799871));
        assertEquals(frobeniusNumber(new int[]{392577, 76122, 819035, 9530, 441032, 479135, 673679, 690641, 785018, 624901}), valueOf(4693959));
        assertEquals(frobeniusNumber(new int[]{259184, 285921, 926921, 225513, 49459, 40747, 89063, 674437, 989010, 774636}), valueOf(3523390));
        assertEquals(frobeniusNumber(new int[]{50853, 757568, 279571, 812191, 840807, 268180, 131331, 758132, 48488, 289176}), valueOf(3818104));
        assertEquals(frobeniusNumber(new int[]{474700, 802781, 739218, 709126, 277847, 546796, 501703, 523270, 168693, 463569}), valueOf(8390108));
        assertEquals(frobeniusNumber(new int[]{440762, 627748, 973229, 599380, 465312, 277549, 431298, 846196, 521329, 994129}), valueOf(10475928));
        assertEquals(frobeniusNumber(new int[]{284494, 107778, 394044, 733165, 559470, 861878, 613740, 994133, 900154, 977149}), valueOf(10297217));
        assertEquals(frobeniusNumber(new int[]{610715, 343044, 262452, 768204, 588714, 247918, 933559, 194028, 833639, 790296}), valueOf(8294263));
        assertEquals(frobeniusNumber(new int[]{728104, 740657, 346035, 800652, 543810, 265471, 183856, 451270, 868752, 256932}), valueOf(7701424));
        assertEquals(frobeniusNumber(new int[]{986901, 388623, 807483, 830753, 777579, 298343, 223983, 975869, 900963, 497430}), valueOf(10639455));
        assertEquals(frobeniusNumber(new int[]{54168, 520193, 913517, 235881, 235471, 715450, 195247, 238840, 777715, 887390}), valueOf(5707347));
        assertEquals(frobeniusNumber(new int[]{846275, 425111, 597863, 576722, 906568, 987701, 369518, 967044, 430564, 802354}), valueOf(12918084));
        assertEquals(frobeniusNumber(new int[]{79207, 548260, 111217, 931719, 333550, 589743, 421776, 958290, 249630, 843988}), valueOf(6270019));
        assertEquals(frobeniusNumber(new int[]{442313, 849206, 942831, 823129, 640085, 430521, 953096, 478084, 506877, 230757}), valueOf(10209207));
        assertEquals(frobeniusNumber(new int[]{320281, 161232, 378867, 848243, 804140, 727268, 331278, 575098, 985322, 925647}), valueOf(9575383));
        assertEquals(frobeniusNumber(new int[]{451252, 90799, 134394, 483581, 691118, 625769, 503651, 663940, 882291, 533868}), valueOf(7110039));
        assertEquals(frobeniusNumber(new int[]{391674, 484185, 16787, 549836, 892074, 657594, 865100, 834420, 993623, 619217}), valueOf(7517776));
        assertEquals(frobeniusNumber(new int[]{30893, 338866, 985217, 68811, 411399, 732470, 713351, 251220, 521989, 579006}), valueOf(4577378));
        assertEquals(frobeniusNumber(new int[]{116493, 903691, 672358, 852921, 385430, 891420, 827679, 244841, 843701, 62174}), valueOf(6967111));
        assertEquals(frobeniusNumber(new int[]{858800, 888052, 736722, 752971, 853757, 643401, 156783, 639, 225930, 269993}), valueOf(3122921));
        assertEquals(frobeniusNumber(new int[]{485847, 53772, 985808, 599349, 309080, 338076, 161204, 931269, 193429, 518269}), valueOf(5773450));
        assertEquals(frobeniusNumber(new int[]{450575, 189662, 682149, 170312, 207991, 601225, 180904, 148159, 960761, 723096}), valueOf(5751395));
        assertEquals(frobeniusNumber(new int[]{264806, 943906, 831629, 824126, 71068, 985648, 757572, 982466, 70151, 682768}), valueOf(8604781));
        assertEquals(frobeniusNumber(new int[]{460934, 634593, 256422, 684988, 460338, 662607, 157446, 581339, 530927, 965682}), valueOf(8385941));
        assertEquals(frobeniusNumber(new int[]{93790, 618944, 641078, 135638, 317890, 970363, 890958, 646648, 825663, 678241}), valueOf(7497291));
        assertEquals(frobeniusNumber(new int[]{622139, 641655, 627549, 238205, 686224, 569640, 635820, 953264, 620757, 420826}), valueOf(10514610));
        assertEquals(frobeniusNumber(new int[]{50421, 93396, 9521, 980613, 419230, 666561, 271124, 861152, 289627, 285041}), valueOf(2503700));
        assertEquals(frobeniusNumber(new int[]{94795, 747723, 493194, 45651, 229649, 457361, 535857, 829610, 913814, 158779}), valueOf(4594093));
        assertEquals(frobeniusNumber(new int[]{313731, 338678, 33194, 498948, 449405, 907018, 946753, 922119, 380419, 669}), valueOf(1957125));
        assertEquals(frobeniusNumber(new int[]{838795, 24709, 205455, 747978, 348071, 544316, 319385, 841032, 112254, 163605}), valueOf(3957746));
        assertEquals(frobeniusNumber(new int[]{544198, 683987, 388190, 981008, 186522, 728340, 681313, 674812, 523116, 8304}), valueOf(5836853));
        assertEquals(frobeniusNumber(new int[]{229789, 955498, 277471, 457471, 873917, 865270, 651197, 252196, 48722, 944481}), valueOf(6389633));
        assertEquals(frobeniusNumber(new int[]{194041, 203501, 976022, 580292, 733968, 793169, 451057, 826137, 875984, 235046}), valueOf(8937829));
        assertEquals(frobeniusNumber(new int[]{804720, 565116, 974345, 477988, 588439, 746526, 936466, 454366, 143777, 314712}), valueOf(9902355));
        assertEquals(frobeniusNumber(new int[]{322129, 291064, 843423, 221857, 622347, 961728, 343063, 472826, 67503, 900037}), valueOf(6580448));
        assertEquals(frobeniusNumber(new int[]{266296, 963935, 289705, 337921, 743134, 279729, 60231, 612211, 530564, 357679}), valueOf(6257893));
        assertEquals(frobeniusNumber(new int[]{224705, 730350, 145199, 761544, 871163, 452480, 750190, 514978, 315978, 668546}), valueOf(8494148));
        assertEquals(frobeniusNumber(new int[]{582287, 124968, 563719, 900937, 96902, 951252, 986702, 365117, 614102, 757479}), valueOf(7782182));
        assertEquals(frobeniusNumber(new int[]{1641, 950826, 838771, 777441, 575099, 671874, 976924, 817379, 112350, 278096}), valueOf(3634795));
        assertEquals(frobeniusNumber(new int[]{201866, 12219, 930864, 693539, 336890, 45363, 207758, 172773, 920738, 576466}), valueOf(2869274));
        assertEquals(frobeniusNumber(new int[]{443465, 724044, 898255, 815925, 562117, 684778, 551118, 761190, 835398, 359186}), valueOf(11827788));
        assertEquals(frobeniusNumber(new int[]{628609, 100713, 402629, 39492, 612677, 70043, 816115, 848459, 937397, 269777}), valueOf(4467066));
        assertEquals(frobeniusNumber(new int[]{795068, 946506, 407579, 599104, 814352, 25011, 10873, 892380, 40719, 44796}), valueOf(1691704));
        assertEquals(frobeniusNumber(new int[]{810324, 217514, 617272, 650679, 733321, 495343, 891774, 657559, 104679, 146244}), valueOf(7101838));
        assertEquals(frobeniusNumber(new int[]{704105, 792173, 636432, 481924, 240669, 975060, 428788, 131326, 126439, 727317}), valueOf(7039113));
        assertEquals(frobeniusNumber(new int[]{355698, 378913, 118843, 330615, 972942, 16952, 53923, 407934, 610344, 492605}), valueOf(3368462));
        assertEquals(frobeniusNumber(new int[]{6939, 974882, 665854, 535217, 63332, 649284, 182283, 952031, 106528, 854307}), valueOf(2920650));
        assertEquals(frobeniusNumber(new int[]{522462, 491680, 619867, 55616, 108996, 222962, 311506, 768095, 920958, 137039}), valueOf(4744878));
        assertEquals(frobeniusNumber(new int[]{151960, 577610, 154748, 692190, 375289, 80486, 135486, 307540, 30583, 474056}), valueOf(2946294));
        assertEquals(frobeniusNumber(new int[]{556295, 598042, 775455, 28649, 741053, 396574, 273549, 315322, 586020, 238093}), valueOf(5464547));
        assertEquals(frobeniusNumber(new int[]{260634, 866703, 35404, 574684, 247443, 944237, 605643, 194827, 915201, 869103}), valueOf(6344958));
        assertEquals(frobeniusNumber(new int[]{75988, 490823, 746824, 648017, 39528, 286661, 282320, 779164, 115594, 243127}), valueOf(3864105));
        assertEquals(frobeniusNumber(new int[]{858608, 201654, 735501, 690054, 997515, 277037, 119954, 255245, 494054, 263681}), valueOf(6429017));
        assertEquals(frobeniusNumber(new int[]{689559, 449459, 133755, 233731, 431722, 89062, 348269, 158406, 422958, 303337}), valueOf(4432700));
        assertEquals(frobeniusNumber(new int[]{241536, 686193, 459662, 888092, 467539, 218813, 677348, 214001, 769306, 67949}), valueOf(5907494));
        assertEquals(frobeniusNumber(new int[]{272002, 643625, 949765, 647414, 346946, 985338, 781673, 291762, 927271, 299811}), valueOf(9994910));
        assertEquals(frobeniusNumber(new int[]{45175, 918864, 294709, 97111, 225683, 39179, 901983, 189736, 970346, 74673}), valueOf(2556978));
        assertEquals(frobeniusNumber(new int[]{972246, 215946, 556745, 655641, 896600, 25476, 400960, 784837, 313037, 257979}), valueOf(5673119));
        assertEquals(frobeniusNumber(new int[]{377530, 59359, 994394, 143760, 233696, 748662, 927475, 852273, 244792, 106592}), valueOf(4669246));
        assertEquals(frobeniusNumber(new int[]{810841, 983863, 164524, 138078, 71611, 637688, 284681, 205571, 520968, 619016}), valueOf(5079675));
        assertEquals(frobeniusNumber(new int[]{607530, 278485, 561202, 648665, 347363, 71480, 872922, 366716, 294463, 774557}), valueOf(6730051));
        assertEquals(frobeniusNumber(new int[]{376274, 208715, 530152, 940347, 107746, 306227, 519393, 673110, 760938, 798149}), valueOf(7437829));
        assertEquals(frobeniusNumber(new int[]{850717, 221991, 606454, 112883, 98047, 704540, 962329, 274466, 513483, 864249}), valueOf(6234608));
        assertEquals(frobeniusNumber(new int[]{413305, 454932, 916949, 441763, 552897, 897282, 834230, 34644, 299660, 93052}), valueOf(5752260));
        assertEquals(frobeniusNumber(new int[]{758241, 632926, 266630, 772972, 883300, 652210, 574762, 250635, 166942, 304054}), valueOf(7977093));
        assertEquals(frobeniusNumber(new int[]{887595, 670317, 347825, 636116, 782825, 950803, 848930, 894449, 994599, 355669}), valueOf(13053150));
        assertEquals(frobeniusNumber(new int[]{194925, 425990, 33993, 396031, 370918, 693021, 487186, 944437, 908454, 809954}), valueOf(6351776));
        assertEquals(frobeniusNumber(new int[]{887097, 217572, 855474, 243913, 923852, 303293, 528405, 39746, 998366, 850873}), valueOf(6511009));
        assertEquals(frobeniusNumber(new int[]{49789, 938281, 648350, 306877, 695579, 669500, 341168, 458920, 374650, 144003}), valueOf(5902280));
        assertEquals(frobeniusNumber(new int[]{179696, 23137, 418864, 439866, 315975, 478838, 100080, 294407, 822335, 464477}), valueOf(3866972));
        assertEquals(frobeniusNumber(new int[]{764969, 126409, 782975, 619346, 15112, 325748, 623333, 817640, 910705, 16020}), valueOf(3566132));
        assertEquals(frobeniusNumber(new int[]{371835, 417701, 44369, 178454, 824122, 735653, 70521, 167655, 23535, 534119}), valueOf(2526141));
        assertEquals(frobeniusNumber(new int[]{478551, 458142, 673525, 818749, 879897, 310846, 599028, 315632, 257406, 745041}), valueOf(9051759));
        assertEquals(frobeniusNumber(new int[]{548310, 584063, 317743, 883819, 420349, 428580, 844975, 381614, 791237, 649851}), valueOf(10047053));
        assertEquals(frobeniusNumber(new int[]{100185, 977999, 151311, 103089, 975144, 626244, 513223, 476775, 558009, 440380}), valueOf(6062996));
        assertEquals(frobeniusNumber(new int[]{355555, 255565, 684693, 955712, 574526, 650206, 744175, 302193, 468884, 329119}), valueOf(8502892));
        assertEquals(frobeniusNumber(new int[]{190141, 764301, 338190, 221103, 784165, 554352, 128487, 68458, 620671, 531316}), valueOf(5078585));
        assertEquals(frobeniusNumber(new int[]{144623, 949632, 281520, 454513, 148968, 346962, 712085, 633716, 208191, 451614}), valueOf(5763943));
        assertEquals(frobeniusNumber(new int[]{652966, 79838, 356438, 179117, 576164, 510898, 387342, 558179, 597384, 905120}), valueOf(6728116));
        assertEquals(frobeniusNumber(new int[]{798982, 89702, 430728, 208112, 824303, 634081, 847984, 109825, 171342, 663785}), valueOf(6484939));
        assertEquals(frobeniusNumber(new int[]{19251, 628952, 270195, 899115, 356896, 311040, 608628, 751479, 125405, 543122}), valueOf(4722866));
        assertEquals(frobeniusNumber(new int[]{707133, 292054, 705545, 169489, 103915, 915838, 365711, 865270, 350833, 625067}), valueOf(6977026));
        assertEquals(frobeniusNumber(new int[]{520093, 412404, 358904, 892108, 143092, 601541, 55215, 559196, 583207, 760513}), valueOf(6314228));
        assertEquals(frobeniusNumber(new int[]{824783, 319056, 612513, 593517, 460777, 341538, 145899, 123879, 68570, 541960}), valueOf(4918789));
    }

    @Test
    public void testMathematica2() {
        assertEquals(frobeniusNumber(new int[]{5956, 388, 8234, 6312, 5379}), valueOf(71845));
        assertEquals(frobeniusNumber(new int[]{5967, 612, 1169, 7841, 196}), valueOf(21530));
        assertEquals(frobeniusNumber(new int[]{1298, 3572, 5729, 6319, 7759}), valueOf(105527));
        assertEquals(frobeniusNumber(new int[]{8735, 3368, 7318, 3788, 8724}), valueOf(143309));
        assertEquals(frobeniusNumber(new int[]{1834, 33, 5646, 2670, 9759}), valueOf(19979));
        assertEquals(frobeniusNumber(new int[]{9378, 4691, 6995, 2132, 7411}), valueOf(138950));
        assertEquals(frobeniusNumber(new int[]{5625, 7146, 5484, 6122, 6501}), valueOf(155929));
        assertEquals(frobeniusNumber(new int[]{3803, 97, 3223, 8644, 6795}), valueOf(34571));
        assertEquals(frobeniusNumber(new int[]{682, 1674, 1131, 4888, 475}), valueOf(18636));
        assertEquals(frobeniusNumber(new int[]{4557, 305, 5818, 660, 4260}), valueOf(31066));
        assertEquals(frobeniusNumber(new int[]{5002, 5591, 9775, 7315, 7075}), valueOf(186475));
        assertEquals(frobeniusNumber(new int[]{1920, 2040, 2847, 215, 9595}), valueOf(29053));
        assertEquals(frobeniusNumber(new int[]{2963, 9818, 1041, 746, 5162}), valueOf(44375));
        assertEquals(frobeniusNumber(new int[]{8362, 2673, 2810, 693, 6056}), valueOf(63663));
        assertEquals(frobeniusNumber(new int[]{1653, 5399, 9658, 7655, 4908}), valueOf(160820));
        assertEquals(frobeniusNumber(new int[]{6408, 1389, 6163, 9847, 1520}), valueOf(75205));
        assertEquals(frobeniusNumber(new int[]{5333, 2604, 678, 5781, 7647}), valueOf(69238));
        assertEquals(frobeniusNumber(new int[]{2182, 9029, 6515, 4810, 9248}), valueOf(134001));
        assertEquals(frobeniusNumber(new int[]{4324, 8377, 5866, 8221, 585}), valueOf(84019));
        assertEquals(frobeniusNumber(new int[]{9600, 7652, 7896, 3603, 8961}), valueOf(228163));
        assertEquals(frobeniusNumber(new int[]{46, 4967, 50, 9208, 6921}), valueOf(6021));
        assertEquals(frobeniusNumber(new int[]{1369, 7201, 5430, 3242, 8735}), valueOf(117323));
        assertEquals(frobeniusNumber(new int[]{6351, 8308, 2919, 7004, 1308}), valueOf(90187));
        assertEquals(frobeniusNumber(new int[]{9227, 1238, 9680, 678, 9425}), valueOf(72111));
        assertEquals(frobeniusNumber(new int[]{7428, 2691, 2191, 4646, 7775}), valueOf(98604));
        assertEquals(frobeniusNumber(new int[]{6401, 1559, 1783, 820, 5761}), valueOf(39377));
        assertEquals(frobeniusNumber(new int[]{5758, 2827, 1790, 1293, 4552}), valueOf(50932));
        assertEquals(frobeniusNumber(new int[]{3002, 1721, 5820, 380, 5810}), valueOf(41371));
        assertEquals(frobeniusNumber(new int[]{3093, 7969, 4095, 7203, 122}), valueOf(34508));
        assertEquals(frobeniusNumber(new int[]{3709, 3000, 1783, 2487, 1430}), valueOf(42635));
        assertEquals(frobeniusNumber(new int[]{8190, 4210, 4645, 3827, 6962}), valueOf(137254));
        assertEquals(frobeniusNumber(new int[]{9030, 6866, 7693, 7714, 8665}), valueOf(224121));
        assertEquals(frobeniusNumber(new int[]{6639, 5409, 3359, 1428, 6133}), valueOf(85063));
        assertEquals(frobeniusNumber(new int[]{7479, 6919, 8802, 5035, 6507}), valueOf(217096));
        assertEquals(frobeniusNumber(new int[]{1347, 8276, 8708, 5550, 8856}), valueOf(129461));
        assertEquals(frobeniusNumber(new int[]{8025, 5276, 4499, 8158, 4267}), valueOf(134029));
        assertEquals(frobeniusNumber(new int[]{875, 3845, 1644, 4706, 47}), valueOf(10692));
        assertEquals(frobeniusNumber(new int[]{2281, 7451, 1367, 8062, 2302}), valueOf(67455));
        assertEquals(frobeniusNumber(new int[]{4408, 3573, 7703, 7720, 7920}), valueOf(161692));
        assertEquals(frobeniusNumber(new int[]{2602, 7265, 9581, 3222, 510}), valueOf(62412));
        assertEquals(frobeniusNumber(new int[]{8171, 597, 4460, 2579, 6280}), valueOf(62073));
        assertEquals(frobeniusNumber(new int[]{855, 6363, 5169, 3740, 9285}), valueOf(82573));
        assertEquals(frobeniusNumber(new int[]{263, 6179, 4328, 1431, 9173}), valueOf(36659));
        assertEquals(frobeniusNumber(new int[]{1123, 6193, 4146, 5364, 6609}), valueOf(92197));
        assertEquals(frobeniusNumber(new int[]{9515, 443, 2831, 3963, 1203}), valueOf(33062));
        assertEquals(frobeniusNumber(new int[]{6191, 8874, 1214, 7510, 4982}), valueOf(106093));
        assertEquals(frobeniusNumber(new int[]{1490, 3259, 4628, 6375, 9500}), valueOf(95052));
        assertEquals(frobeniusNumber(new int[]{5295, 4104, 5665, 3720, 3851}), valueOf(109364));
        assertEquals(frobeniusNumber(new int[]{5937, 5185, 5810, 5375, 7103}), valueOf(149800));
        assertEquals(frobeniusNumber(new int[]{2272, 4029, 6633, 1038, 8580}), valueOf(75386));
        assertEquals(frobeniusNumber(new int[]{7593, 1867, 1765, 1606, 353}), valueOf(38321));
        assertEquals(frobeniusNumber(new int[]{5493, 6354, 79, 4224, 4396}), valueOf(37039));
        assertEquals(frobeniusNumber(new int[]{7888, 1821, 971, 5114, 77}), valueOf(16205));
        assertEquals(frobeniusNumber(new int[]{3060, 499, 4268, 617, 1309}), valueOf(20212));
        assertEquals(frobeniusNumber(new int[]{8985, 2457, 9795, 4636, 9891}), valueOf(176696));
        assertEquals(frobeniusNumber(new int[]{5837, 1555, 1537, 5289, 334}), valueOf(28868));
        assertEquals(frobeniusNumber(new int[]{9424, 1702, 5115, 1964, 6206}), valueOf(97005));
        assertEquals(frobeniusNumber(new int[]{9223, 2999, 633, 7129, 8399}), valueOf(111398));
        assertEquals(frobeniusNumber(new int[]{2044, 6614, 4573, 2736, 7307}), valueOf(110059));
        assertEquals(frobeniusNumber(new int[]{6641, 9604, 1629, 7712, 4781}), valueOf(141851));
        assertEquals(frobeniusNumber(new int[]{7445, 2073, 3237, 1439, 4003}), valueOf(69240));
        assertEquals(frobeniusNumber(new int[]{3207, 6914, 2634, 8724, 8929}), valueOf(138529));
        assertEquals(frobeniusNumber(new int[]{2724, 3040, 1468, 2859, 1697}), valueOf(46427));
        assertEquals(frobeniusNumber(new int[]{5229, 9992, 7410, 4013, 3319}), valueOf(129864));
        assertEquals(frobeniusNumber(new int[]{30, 1069, 2074, 6704, 9517}), valueOf(11729));
        assertEquals(frobeniusNumber(new int[]{3332, 723, 1353, 2490, 5063}), valueOf(37632));
        assertEquals(frobeniusNumber(new int[]{100, 6744, 4269, 8804, 9449}), valueOf(48072));
        assertEquals(frobeniusNumber(new int[]{8317, 4582, 1815, 5099, 4549}), valueOf(120805));
        assertEquals(frobeniusNumber(new int[]{7283, 9524, 4133, 6444, 8527}), valueOf(202650));
        assertEquals(frobeniusNumber(new int[]{2753, 4279, 6561, 1757, 9254}), valueOf(109694));
        assertEquals(frobeniusNumber(new int[]{5776, 8811, 5672, 7993, 9566}), valueOf(199733));
        assertEquals(frobeniusNumber(new int[]{9609, 5355, 9034, 6122, 798}), valueOf(109873));
        assertEquals(frobeniusNumber(new int[]{1181, 649, 7845, 248, 5369}), valueOf(17754));
        assertEquals(frobeniusNumber(new int[]{4652, 5316, 9689, 3508, 782}), valueOf(71819));
        assertEquals(frobeniusNumber(new int[]{4851, 3229, 2055, 9635, 432}), valueOf(43718));
        assertEquals(frobeniusNumber(new int[]{5919, 3008, 4686, 4938, 6103}), valueOf(120219));
        assertEquals(frobeniusNumber(new int[]{2679, 1060, 8406, 4127, 8258}), valueOf(80356));
        assertEquals(frobeniusNumber(new int[]{8273, 3708, 1278, 7941, 5439}), valueOf(97591));
        assertEquals(frobeniusNumber(new int[]{1732, 3120, 5931, 2623, 1886}), valueOf(64576));
        assertEquals(frobeniusNumber(new int[]{3455, 6557, 8769, 5280, 8134}), valueOf(150984));
        assertEquals(frobeniusNumber(new int[]{8857, 9450, 5603, 1726, 8911}), valueOf(164662));
        assertEquals(frobeniusNumber(new int[]{69, 634, 300, 8382, 2080}), valueOf(5147));
        assertEquals(frobeniusNumber(new int[]{5695, 320, 7289, 8486, 9359}), valueOf(94311));
        assertEquals(frobeniusNumber(new int[]{4918, 6295, 3985, 7622, 7930}), valueOf(145171));
        assertEquals(frobeniusNumber(new int[]{9409, 467, 6185, 410, 8372}), valueOf(69815));
        assertEquals(frobeniusNumber(new int[]{546, 7779, 6215, 4133, 9180}), valueOf(87784));
        assertEquals(frobeniusNumber(new int[]{6029, 1942, 7477, 7257, 3273}), valueOf(119364));
        assertEquals(frobeniusNumber(new int[]{1472, 4719, 2611, 5558, 842}), valueOf(59267));
        assertEquals(frobeniusNumber(new int[]{2648, 6600, 6086, 3781, 6202}), valueOf(119635));
        assertEquals(frobeniusNumber(new int[]{5607, 1907, 6900, 5813, 4255}), valueOf(105893));
        assertEquals(frobeniusNumber(new int[]{1923, 3735, 3440, 6244, 4109}), valueOf(79309));
        assertEquals(frobeniusNumber(new int[]{237, 7428, 5937, 2702, 5520}), valueOf(57571));
        assertEquals(frobeniusNumber(new int[]{6208, 8280, 317, 8293, 2769}), valueOf(54946));
        assertEquals(frobeniusNumber(new int[]{109, 1651, 6793, 3625, 8111}), valueOf(30633));
        assertEquals(frobeniusNumber(new int[]{7555, 5326, 609, 9292, 3676}), valueOf(80669));
        assertEquals(frobeniusNumber(new int[]{8577, 5469, 1366, 2710, 5168}), valueOf(78305));
        assertEquals(frobeniusNumber(new int[]{5058, 1116, 3922, 3617, 6208}), valueOf(75031));
        assertEquals(frobeniusNumber(new int[]{6907, 1057, 2189, 5134, 1810}), valueOf(57079));
    }

    @Ignore
    @Test
    public void test2() throws Exception {
        int[] A = {1312132131, 423123122, 12312365};
        assertEquals(frobeniusNumber(A), valueOf(5397139628599L));
    }

}
