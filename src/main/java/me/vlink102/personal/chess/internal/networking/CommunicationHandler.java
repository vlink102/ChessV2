package me.vlink102.personal.chess.internal.networking;

import me.vlink102.personal.chess.ChessMenu;
import me.vlink102.personal.chess.internal.networking.packets.Online;
import me.vlink102.personal.chess.internal.networking.packets.RequestOnline;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Objects;


public class CommunicationHandler {
    @SuppressWarnings("SpellCheckingInspection")
    private static final String V = "ZF16eYBBOuatpyxqOIHg";
    private static String user;
    private static String host;
    private static String dbName;
    private static int port;

    public static DataThread thread;

    public CommunicationHandler(String user, String host, String dbName, int port) {
        CommunicationHandler.user = user;
        CommunicationHandler.host = host;
        CommunicationHandler.dbName = dbName;
        CommunicationHandler.port = port;
    }

    private static final String pws = V;

    public static String nameFromUUID(String uuid) {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `PLAYERS` WHERE `uuid`='" + uuid + "';");
             ResultSet set = statement.executeQuery()) {
            if (set.next()) {
                return set.getString("name");
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static String UUIDfromName(String name) {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `PLAYERS` WHERE `name`='" + name + "';");
             ResultSet set = statement.executeQuery()) {
            if (set.next()) {
                return set.getString("uuid");
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getAge(String uuid) {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `PLAYERS` WHERE `uuid`='"+ uuid +"';");
             ResultSet set = statement.executeQuery()) {
            if (set.next()) {
                return set.getInt("age");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLocation(String uuid) {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `PLAYERS` WHERE `uuid`='"+ uuid +"';");
             ResultSet set = statement.executeQuery()) {
            if (set.next()) {
                return set.getString("location");
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAboutMe(String uuid) {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `PLAYERS` WHERE `uuid`='"+ uuid +"';");
             ResultSet set = statement.executeQuery()) {
            if (set.next()) {
                return set.getString("about");
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProfilePicture(String uuid) {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `PLAYERS` WHERE `uuid`='"+ uuid +"';");
             ResultSet set = statement.executeQuery()) {
            if (set.next()) {
                String pfp = set.getString("pfp");
                if (pfp.equals("")) {
                    return "iVBORw0KGgoAAAANSUhEUgAAAQAAAAEACAYAAABccqhmAAAAAXNSR0IArs4c6QAAIABJREFUeF7tXflzFNe1Ptr3FRBCEtggwDYIm80YEGYxSBgMCouXV/7/kspLKpWU7fDiVF5VqpJXLz/Ez2bVrtHMSLOPpFmkWbS9+hoLCyHN9HJvd0/3OVUUFHPvued+t/vru5x7Tllw1r9OLIwAI+BKBMqYAFw57txpRkBBgAmAHwRGwMUIMAG4ePC564wAEwA/A4yAixFgAnDx4HPXGQEmAH4GGAEXI8AE4OLB564zAkwA/AwwAi5GgAnAxYPPXWcEmAD4GWAEXIwAE4CLB5+7zggwAfAzwAi4GAEmABcPPnedEWAC4GeAEXAxAkwALh587jojwATAzwAj4GIEmABcPPjcdUaACYCfAUbAxQgwAbh48LnrjAATAD8DjICLEWACcPHgc9cZASYAfgYYARcjwATg4sHnrjMCTAD8DDACLkaACcDFg89dZwSYAPgZYARcjAATgEsGf319nZaXl2llZZlWV9dobX2N1tfWicrWqYzKqby8jCoqK6myopIqKyupvLzcJci4u5tMAA4df7zwq6urlEgkaCG5QNlMRlNP6+vrqbWllZqamqmiooLKyso01efCpYEAE0BpjJMqK/HSLy6mKZFMUiaToXw+R/g/I4IXv6amhkAIDfWN1NDQoBACizMQYAJwwDjiS59eTFM4FKTllRWpPUqnFmnPnt3U1dWtEAPPDKTCLV05E4B0iOU1gK97fC5OCwvzlMvl5DW0RfPS0hKFQ2Hq7t5PfX3HqaqqyrS2uSGxCDABiMXTFG148TOZJQqFQ8pU3wrJ5/M0MT6pLAc++ugcdXbu46WBFQNhsE0mAIMAWlE9EAxQIrFAa2trVjT/qk2QwNjouHKy0N3dQ5cvX+bTA0tHRHvjTADaMbOsRjabpVAoSItLiwVtKCsrp5qaaqqprqHq6mqqqqx6tZOP2QOIY2VlhfLLecrl85TLZZUTAz2C5QBmAjhibGtto/MXztOePXv0qOI6FiDABGAB6HqazOVz5PFMFX1RG+oblBdRyzk+SGEhsUCpdEqPaZROp5WZAPTAh+DaJ9doX+c+Xbq4krkIMAGYi7eu1hLJBAUDs7RaYMpfW1NDLc2tys68HsFpIU4S5hfm9FSnUDBEs7MBpW5FZQWd/+g89fb26tLFlcxDgAnAPKx1tYSvst/v2/E8H8dwjQ2NyldfhESiEcrmsppVra+v0Yvno5TNvtyUhF39/Zfo4MG3+ahQM5rmVWACMA9rzS1hrY+Xv9D6vK21lRobmoS9ZPML87qXAtgUHBkeVfYDIDgexMZgd3e35r5zBXMQYAIwB2fNrWSzOfJMT+64048vbEtzCzU3NWvWXagC9gKSqaRunYHZAAWDoVf1sSfw6aefUnt7u26dXFEeAkwA8rDVrRm79F7vNC1llnbUsTHtF+2JF4vHCrZbrFMryyv07Nnz12YtePlv3rzJDkPFwLPgdyYAC0Av1CR20sOREMXj8R2L1dbU0p7de4RN+zcaQtuBUKDoSUMxyGLROHm93teKHT16lM6dO6fpdKJYO/y7cQSYAIxjKFRDKpUkn99XUGfn3k6qrqoW2i6UzSfmKZXSdxS42RjsWWAvAH4LG4KZypUrV+jAgQPC7WaF+hFgAtCPnYSa6+TxeApOwXE9t62lVXjbcAiKRMOGbw9uGOb3zVAkEnnNzvZdu+iz27eFz1yEg+EihUwANhpsXOyBp99OUlVZSfs6u6RYHI6GhV4oWlrK0PCL4Tds/fDDs/Tee8ek9IGVakeACUA7ZlJq4OhsYnKC1tZ2dsnFrj/+iBSs++cW5pU4AqIFy4DFxdfdlnE0ODR0lxoaGkU3x/p0IMAEoAM0GVXm5ucoGHzpSbeTdHV2Ka62IgVrfqz9ZUg0GiOf9839jLNnztKx4zwLkIG5Vp1MAFoRk1QeX39cytlJ6mrrlJ1/kYLzfrgZG40atJNNmaUMjYyMvuHL0NraSkNDQyK7wrp0IsAEoBM4kdXgg49z/0KyZ3cH1dXWCml2I3TYfGJB2ssPQ5eXV2h0ZHTbvYWBgQHat48vDAkZUANKmAAMgCeq6rR3uuAaHDf7MP3XcsOvkG1G3H219BlEg30AXBneKnv37qXBwUE+EdACqISyTAASQNWiEpt/U55J5X7+ToKNs86OTsMvC9pAhODtXkgtNmsp6532Uiz2plNTXV0dffbZZ0qwURbrEGACsA57peWlpUXCDKDQOhxXfDt2dxgiALz8kVikINHIgCIcjtCMf+YN1eUV5TQ4MEgdHR0ymmWdKhFgAlAJlKxisViUwpFwQfVGXH9BLMlUSrnggyu7ZgvyEiBi0HZy+vRp6uvrM9skbm8TAkwAFj8OXt+0ElGnkCDYBzYBtV78Qay+WDyuhP6ySnAS8GIbhyDYs3//frp27ZpVpnG7iNsQnPUbyxzBMBpCYGR0hFZXC8fy17oHgNuEuNaL24RWBw5FuPJnT59vi1FDfT09/PxzQ/hxZWMIMAEYw89QbWwAjo2PFtWBL39nx16qKnIBCJdw8NLj5Zd1tl/U2C0F0Mcnj5/uWO3LL7+kWkHHm1pt4/I8A7D0GcBuvGd6SpUNiOzb0bGXKrZJ2omXDOv8bC5j+CqvKmM0FEKUoKdPnu1Y4/Znt2n3rt0aNHJRkQjwDEAkmhp1JZMJ8s/4VdfCFeCmxibCDjoy+2Jtjyu3Vq7xixmfyWbpxbMXOxb75JPr1NPDIcOK4SjrdyYAWciq0Ds3F6dggdt/KlTYvkgqmaaxsbEd7bx48SIdPnzY9v1wqoFMABaObLHrvxaaJqzpnS4EbTRw/vx5QrQgFmsQYAKwBnelVYT9CoV3vv9voWnCmp6ZmaFw6PXAIJuV91+8SL08AxCGt1ZFTABaERNYfn5+jpDnz6mCk4ixsXFKp3b2c7h06RIdOnTIqRDYvl9MABYOEbzzEPffqQL3Y1wGKpS6/PqNG9TdJSfKkVNxFdkvJgCRaGrUpeUYUKNqWxTHCQUIoFBik3v37lFzs9jcBrbofIkYwQRg4UDhCzk6NmKhBXKbxh7HtOf18OCbW4SD09dff61kLmaxBgEmAGtwf9UqCKDQVWCLzTPUPIKBpNM7pzLHlx8zABbrEGACsA57pWWf3yskFr/F3Xij+aXFJRoeLjy7OXLkMF24cNFuprvKHiYAi4fbqUeBCAYKH4BC0t/fzynELX7+mAAsHgA1AUEsNlFz81jSDL8YIdwD2EkQ3Rj5Anft2qVZP1cQhwATgDgsdWnCyzI5hZBgL1NqO0FCoRDNzhT2b2hobKQ7d+5QTbX4FGdOwNCsPjABmIV0gXaQCxA5AZ0gO4UC39q33t5ewhKAxVoEmACsxV9pHRGBEBnICTLtmaZ4fK5gVxDd+LPbn1Fbe5sTulzSfWACsMnwITJwJpOxiTX6zJifnyfPVOEAp9Dc1dVFN27c0NcI1xKKABOAUDj1Kyv1ewFqvP6ADpx/+uH/f/CgfrC4pjAEmACEQWlMETYDxyfGLI/hp7cXY6PjqvwZqqur6eHDh4Q4hyzWI8AEYP0YvLIAGXsCgVkbWVTcFAQd9Xn9ytXmYoK1P6IAd3dzBKBiWJn1OxOAWUirbAcxAs3M3KPSrB2LhYJBmp1VF9OA1/5G0RZfnwlAPKaGNC4uppVMQXYX3PUPBoIUCoVVRSDG13/w5iB17OFMQHYaWyYAO43Gz7aEwiGKxwu70VppNqb9ePFBAGoEG38fnj1L7773npriXMZEBJgATARbbVPKutrvpcXFnW/SqdUluhy+/LjiOzdX+Kx/c7tY81+9epWv/YoeDAH6mAAEgChDBaLowDkIMf/tIvBTwMuvZY+ipaVFSQOObMAs9kOACcB+Y/LKomw2o+wHFIqoY5b5+OL7fTOaYhcg4w9e/tbWVrPM5HY0IsAEoBEws4snk0kKhgKaXjyRNsLBJxQMK1N+LenGGhoaFF//zs5OkeawLsEIMAEIBlSGOlwZ9nq9tGZyeu9oJEozM7OanZP4qq+Mp0COTiYAObgK1woSCAaDlM1lheverBDLjfn5BYpGo4SoPlpl9+7dhGQf7e3tWqtyeQsQYAKwAHS9TSKN+LRvmrIZ8SSAkwfE7/P7/Eq+QT2ye/ceunHjOsHdl6U0EGACKI1xUqzE1zkcCRFu3YmWZDJFE+MTmtb5m22Ao887R4/SBydPMgGIHhyJ+pgAJIIrSjU233K5rJJFSOaVYSwxgoGQbhJAfxHiCwk/sfMPByAWeyPABGDv8VFexnAkTLgujGm6bMESIBKJGmoGN/2OHDlCZ86cYRIwhKT8ykwA8jHW1cI6rSuegOFwSPeaXE/DIJmpySlKJIyHKGtra6MzZ8/Svs5OJgI9g2FCHSYAE0DW2gS++pjuJxILhqbjWtvdKI+9hvGxcVrUcQqwtU1k/UH8v3PnzhH2CVjshQATgL3GQwmqEYlGCF6AVgr2GjATyGZzQszAseCpUyepu7tHiD5WIgYBJgAxOBrWgql3IBSgxMKCYV2iFGQyWRodHaPVlRVRKqn3cC99dO4jgrMQi/UIMAFYPAaY7iNNeCwale7ko6ercApCpF+RG5DYGzhx4gS99dZbvDegZ1AE1mECEAimVlWIAxiNRTRdrdXahojySPGF0wEtdwGKtYsjwnffeYdOfPAB1dbUFCvOv0tCgAlAErDF1GKDD2v9Qumziukw6/f1tTWamQ1QJBwR3iQyBH/wwQd0kKMEC8dWjUImADUoCSyD+/2RSIQWEuK9+QSaua0qz5SH5ubk2A2/gffff59wi5DFPASYAMzDWjnWQ7gvTP1LUVZWVmlkZIRygk4GtmJQV1tHH370Ib391tulCE9J2swEYMKwIbpPLBalRDIhdB1tgulvNAHyQg4AWS7J8BU4ePBtOnHifcLygEUuAkwAcvFVvvqzgdmSf/E3w7S0lKGR4RGpfcIxIa4VHzp0SPIIuVs9E4Ck8cdXPxINK449InfPJZmrWS1uD05NTdHqyqrmumorYDZw4MABZW+Aw4qpRU1bOSYAbXipKo2XH8k+RZ6dq2rY5EKzMwEKhULSW0V8gbt37/IGoQSkmQAkgIqoucjw43TBaQYChZohDx7cp8bGJjOaclUbTAAShjuby9Hk5LgEzfZSidgBgUBAulFwGvr88885tLgEpJkAJICq7JSPjzpy7b8ZLhGxA9TAjyXA/fv3qYY9BtXApakME4AmuNQVxnXaicnxkj3vV9dLoslJDy1ICE+2tf3Gxka6c+cOhxpTOzAayjEBaABLbVElqIZnSgnj5VTByQb8AdLptPQuItLwzZs3ObWYBKSZACSAipfD5/dROp2SoN0eKnH8NzIyakq0ItwavHz5Mt8clDD0TAASQIVKhPKK2TjDr9Fu4xLT8IsRU5Y5iC14/PhxoyZz/W0QYAKQ9Fggnt+01yNJu/Vq06m0EizEDPnV0K+opbXFjKZc1wYTgKQhXyeisTFzvpCSulBQrc/nJ6QOky3tbW105+5d2c24Vj8TgMShxxIASwGnCfY4njx+asr0v7//EvX28n0AWc8QE4AsZIkol8+TxzNpi/TeIruJnf/REfnT//q6erp1+xa7AIscvC26mAAkgvvyNMBrylGZxG68oRoZg8OhsPQmD/Ueov6L/bz7LxFpJgCJ4EI1svl6PFOOuRgEUhsdGRWSM6AQ9PD6g/MPRwiS+4AyAcjFV9GOMGAibwYiGMdf//q9CZZv30Qul3/DzXlgYFCor351VRXVN9Rb1ke3NMwEUIIjjRgDf/zjH21l+YMHDwguuyylhQATQGmNl2JtMpmkP/3pT7ayHJd1mpr4uq6tBkWFMUwAKkCyW5GFhQX65ptvbGXWvXv3OIafrUZEnTFMAOpwslWp+fl5+vbbb21lE3vr2Wo4VBvDBKAaKvsUjM/F6c/f/dk+BhHR0NAQx+2z1YioM4YJQB1OtioVi8Xo0aNHtrLpzp271N7eZiub2JjiCDABFMfIdiUQi++HH/6tnMXjSNAOUYfhsbdn9x7bYcUGFUaACaAEn5BwJEKx2Ms8fXj58/llymWzhFiEiEiMP9lslvLbnNfL6u7g4CB1dnbKUs96JSHABCAJWJlqsQkYCM4WbQKhyZDeOx6L0+LSIq2v4Y6iHLl27Rrt379fjnLWKg0BJgBp0MpTjE3AUCioqQF4IyKjz2I6TXPz88Lz+128eJEOHz6sySYubD0CTADWj4FmC6LRiJJaXK9g2ZBIJCkYCCpLBRFuyqdPn6a+vj69JnE9ixBgArAIeCPNBoMBmpufM6JCqYsXHxuJIAK4FxuRY8eO0dmzZ42o4LoWIMAEYAHoRpv0z/gUd2CREolEFSLQm7r84MGD9PHHH4s0iXWZgAATgAkgi2wC03fEGkT6MdGCW36TE5O6Un/jBGBgYIDv7oseFMn6mAAkAyxaPQhgfGJMuWIsQ7AnMDU5RZmMtpwGzS3NNHR3iJDRl6V0EGACKJ2xerVuHx4ZhgeANMuxDHjxfFgTyZRXVNB/fPUVVVZWSrOLFYtHgAlAPKZSNaZSSSXpiGwJh8M04y/ua7DZjuvXP6Hu7h7ZprF+gQgwAQgE0wxVMjYAt7NbT+Rf+AHAH4CldBBgAiidsVKO7UZGR2h9fc0UqwOzAQoG1Yc1RxbfL774gnP4mTI6YhphAhCDoylasPPvmZ4ypS00kkqmaGxsXFN7t2/fJiTzZCkNBJgASmOcFCvj8RiFTEw0AtdhRADW4il47tw5evfdd0sIVXebygRQQuPv83kpZWLGYVw1HhnWRgDI5HvlypUSQtXdpjIBlMj4r66u0dj4iKavsdGuLS0uKSnAtcQbqKqspC+/+or3AYyCb1J9JgCTgDbaTCQSpmhMfjLOzXbOxefJ49Ge4fjkqVP0/okTRrvM9U1AgAnABJCNNrG6tkbj46Om5xj0ef0UjWonnZqaWnrw4D5VVVUZ7TrXl4wAE4BkgEWoT6dTivOPlqm4iHbhDYh9AK0Cd+AbN25whCCtwFlQngnAAtC1Nun3+yiZEnv7r5gN2Pn/8f9+KlZsx9/5dqBu6EytyARgKtzaG8MXGGf/Zn/90+lF5QhQr2AWAJ+A9vZ2vSq4ngkIMAGYALLeJvDS4+tv5tHfhq2ID+D3+fWartTb39NDV65e5RuChlCUW5kJQC6+hrRbmVrc6/VRLBozZD9cg2/fuk24KsxiTwSYAOw5LsqUHxt/2AA0W5SYA+MTiiuwUcFewKVLlzhQiFEgJdVnApAErFG1iWSCZmaMTcH12oBw4vAARHAQEYITga6uLhGqWIdgBJgABAMqQh124LHxJ+oF1GoTAoI8e/pcmN/Bno49NDAwSJUVFVpN4fKSEWACkAywHvXRWISQ/ssqwa3D4RcjQpu/2H+RDvdy3gChoApQxgQgAESRKjK48uv1mH7st7kPoVCYZme0RQMqhgFChd28eZN27dpVrCj/biICTAAmgl2sKWy+eX1eWlxMFysq9ffxsXFKCtgA3GpkT08PIYVYWVmZVPtZuXoEmADUYyW9ZCgUovicsaM3o0aChH768Sdak5BHEC/+yZMn6QRfFDI6TMLqMwEIg9KYIhz34etvtWDj8fmzF9LMAAlgQ7Czc6+0NlixegSYANRjJa0kdv0npyYon89La0Ot4oWFBZqckBt2rLm5mYaGOIeA2jGRWY4JQCa6KnTjzB3uvkjfbQeRsQG4Xb+6e7rp8seX+cqwxYPOBGDxAFgR6KNQl73TXorF4qagcubMGTp+/LgpbXEj2yPABGDhkxGPxykcCVl65Le1+6OjY5ROmXMKgRuDH547R0ePHOGTAYueQyYAi4BfXFwkn99raow/NV198uQZLZu4F4ELQ9dv3KA9HEpczfAIL8MEIBzS4gqx4z8zOyPM1bZ4i+pKwAX4yeMntC4v7eC2htTW1tKVy5dpb2enOkO5lDAEmACEQalOUS6fI5/XS/ll63f8t1qsNwioup4XLtXS0kLXr1+nxsZGEepYh0oEmABUAiWiWC6XUy75YOffjuKZ8tDc3LxlptXV1SnuwjgmZDEHASYAc3AmvPwzs37LbvgV66aeZKDFdOr5HXcFkFiEZwJ60NNehwlAO2aaa+TzOZry2PfLjw7BCQlXgM2OPbgdmNgTQDxBJgHNj5rmCkwAmiHTViGTzdDs7IwyA7CzpFJpwiUgOxAAcGptbaX+/n6+PSj5oWECkAgwYvp5vdOE3XW7SzwWp+lp6+8ibMYJewIDAwMKGbDIQYAJQA6ulEgsKJl8S+HlBwQzM7MUDoUloaFfLUgAGYeRdJRFPAJMAIIxxRQ6mUxSIDhrOyefQl0dH5tQ7LajIMXYpUv91N3dwyHGBQ8QE4BgQBHKKxaP2mYtrbZ7Tx4/peXlZbXFTS9XUVFBx44do1OnTpnetpMbZAIQNLqY6kejEZqbnxOk0Tw1OAF4+uSZeQ0aaOnQoUN09uxZwkkBi3EEmACMY6is872+adue8RfrYjweo2mPr1gx2/yOdGM3BgaotqbGNjaVqiFMAAZHLpVKUjAYpOUV+06fi3XRMzVNc3OlNXNpqK9XbhIeOHCgWPf49wIIMAEYeDyisSjhPn8py/r6Gj1/Nmx7P4WdMMaeQF9fH18n1vkQMgHoAA6+/Djiw1GfXRxndHRDqYL1P7IA2XkDsFDfEFPg7bff5n0BnQ8AE4BG4JQQXjM+wn1+Jwj6MTY6XlJHltvh3tHRQVevXuXNQY0PJROABsDwtUQQD7u79WrokrL2xx6AEwS3CHGRqK2tzQndMaUPTAAqYV5ezpN/xk+ZTEZljdIo5vf5KRKJloaxKqzEy//JJ59QQ0ODitJchAlAxTOA9fG012OLsN0qzNVUBDkAkQvQSdLU1ESDg4NMAioGlQmgCEhY8+M2XyqdUgFnaRVZXVmlx48fmx4CzAyU9nXtoyuXrxBiDrLsjAATQIGnAzv8WPOn0+ZEyTX7QU0kkjQxPmF2s6a119PdTVevXeP7AwUQZwIoAE58Lk7hsL3Cdot8e2ZnAxQKhkSqtJUuHBGePnWajh0/Ziu77GQME8AOo4F1MeL3OVUwu8ENwFTKeUubrWN2c/Am7eVchNs+ykwA28CCXH14+ZEo06mCvY3RkTHHnWpsN15tra306a1bnIZsG3CYAN4AZZ3CkTDFYtam6ZZNPPBpwAlAqQQsMYrHe++9p3gLIjsxyy8IMAFseRrSi2ny+bwl7+Jb7CHHxiZmAG4RvPjwD+ju7nZLl1X1kwlgC0w473eKm2+hJ8Dvn6FIOKLqIXFKoc7OTsU/gIVnANs+A8lUgvx+v+OfD2wAPn78lFZLIFipyMHALODjjz9WLg+xvESAZwA/PwlYC09OTbhiTYzYfzgBcKPU1NTQ0NAQIdgoCxPAq2dgfn6OAsGAK56JaY+XkJrcrXL+/Hk6evSoW7v/Wr95BkCkbPhNeSYdfey3MeqrqwgA8rxk7/+LeGtxYejOnTt8IsBLgJePE6bEuOPvBvF6fRSLOvuIU804IutQb2+vmqKOLuP6GYAbnH42nmB4/b1M/+XoZ1pV5zALuHXrFlVWVqoq79RCricA3O/H0R+IwMmC/o2PT1A65cyLTVrHrrKqkgYHBmn37t1aqzqqvOsJIBQJUdzhXn/Y4/BMTtH8QsJRD6/Rzpw48T6dOnXSqJqSru9qAsBX8cXwc0dvBq2trVMwEKBwOOJ470atbyLSj9+79ysqL6/QWtUx5V1NAPD3x31/nA07UUBwU1PTlFhYcGL3hPTp+vXrrnYPdjUB/PDvH2h4ZJiamhqprb2NWltaqMIhm0KL6UUKBAKUTDr/uq8RJkCqsf5Ll8itV4RcSwC4Dvv999+/cesPm0KdnXsJm0QIKFFqt8fQr3gsTgj24fSNTSMv/kZdnAZ8+umnrr0q7FoCyCxl6Ls/f7ftfXi89FgW1NbVKrODpqZmqquzdzJKhCrH+f78/IKjwpaLeMkL6aiqrqa7d+4Q9gPcKK4lgGg0Sn/5y19Ub4zhvBjRZpubmxQ/8orKCkLK6oryciqvMHcTCbv6+NLj/kImk1XcepOJpOq+uPFBL9RnJBRxa45B1xLAjz/+SE+fPtX1LmCGgOWBQgAV5cq/EX22urqGamrwd7Wyl1BZWaE4mqCc3qUEpvEIS76cz1Mmm6NsJquE8UYy0pXlFYUIWIwh0Hv4MPVfvGhMSYnWdi0BfPftdzQ3b15GXGU/gYjKK8qVRwWEUFb28t+bBV93JOzE8R3+zet4+W9WXX0dff7wc90kLd9CeS24kgByuTz97nf/yVNmec9VyWm+f/++ssRzm7iSACKRiLL+Z2EENhBAuLCenh7XAeJKAhgbG6d//et/XTfY3OGdEThz+gwd7zvuOohcSQA//N8Pyp14FkZgA4EjR47QhQsXXAeIKwngn//4B015PK4bbO7wzgjs37+fcByo97SmVLF1HQFgZ/1v//03CgaCpTpmbLcEBDo6OujmzZtMABKwtZXKtfV1+q9Hj1wdE89WA2ITY1paWuju3buuSyTquhkAztW/+eYbJQwYCyOwgQC8Ox8+fMgE4PRHAgTwhz/8wRU58Zw+liL7V1VVRV9++aXitekmcd0MAK6zv//97wm58VgYgQ0EsPn39ddfMwE4/ZEAAfz2t79lH3qnD7SO/n3xxReuSxjiyhnAb37zG3YD1vGCOL3Kg/v3qdFl7sDuJIBf/5o4MrbTX2ft/cMpAAKEuEncSQA8A3DTM666rwgM0tberrq8Ewq6jgBwCvDo0SOamzPvKrATHhSn96GhoYHu3bvHm4BOH2j0z+v10j//55+0usLBNNww3sX6WFZeRufOnqN33n2nWFHH/e66GQBGEO7AwVCI/v73v9PK8rLjBpU7pB4BBGq5eOECHTx0yHVuwEDJlQSgPB7rROFImH766SdCfACQAou7EGhvb6eTJ08qeQHcdgnolf9DcNbv6icfL/7qif4YAAADKUlEQVTExAT9+4cflLh7LM5HAN5+x/v66ERfn+vW/FtH170zgC1IINDm7OwseTweCoVCzn8LXNjD1tZWOnz4MOHqrxvDf2035EwA26CClGHPnj2nWCyqxNjnyLulyRZY3yO/A872jx07Rl1dXaXZEYlWMwEUADebzVIqlVJSbM3MzChHh7xXIPFpFKAaa3lc7cW6Hn+ampuovq7etWv8YpAyARRDaNPvmA0Eg0FlqZBIJAgEgT9I0MFiPgKVFZVUU1tDtbW11NTYSHs7O6m7q8t17rxGkGcC0Ineygoy8ywrSTuSqSSFQ2GKhCNKrgEmBJ2gFqmGzbvWllbq2NtBnZ2dyr+RwxFXeY0kX5FjbWloZQIQPE5YImCmgKUDgo6k02nl3/g/XEHO53OUzy1TfjnPews/Y/9LZqWXWZVqqmuourZambojZx/+wFMPG3duu68v+PF8Qx0TgGyEf9a/sXew+e9sJqMsJRLJpJLuaymzRJnFjPL30tIiYZbhBMFLi5cZGXjq6+upvq7u5Qvd3EzNzc3KvzfO4bf+7YT+27kPTAA2Hh0lASjyAq6sKEsN3GPA/+HPL/9GGrGX/7fx/xskgzKbCWd1de1Vb1Hn1YZmWRlVlP8SCaei4vW0ZRtf3V9yIiIv4sv06ahXUYn8iBW/5EssL6fKqiplal5VWaUkUmWxJwJMAPYcF7aKETAFASYAU2DmRhgBeyLABGDPcWGrGAFTEGACMAVmboQRsCcCTAD2HBe2ihEwBQEmAFNg5kYYAXsiwARgz3FhqxgBUxBgAjAFZm6EEbAnAkwA9hwXtooRMAUBJgBTYOZGGAF7IsAEYM9xYasYAVMQYAIwBWZuhBGwJwJMAPYcF7aKETAFASYAU2DmRhgBeyLABGDPcWGrGAFTEGACMAVmboQRsCcCTAD2HBe2ihEwBQEmAFNg5kYYAXsiwARgz3FhqxgBUxBgAjAFZm6EEbAnAkwA9hwXtooRMAUBJgBTYOZGGAF7IsAEYM9xYasYAVMQYAIwBWZuhBGwJwJMAPYcF7aKETAFASYAU2DmRhgBeyLABGDPcWGrGAFTEPh/xLHd8fQ9n1UAAAAASUVORK5CYII=";
                }
                return set.getString("pfp");
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPass(String uuid) {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `PLAYERS` WHERE `uuid`='"+ uuid +"';");
             ResultSet set = statement.executeQuery()) {
            if (set.next()) {
                return set.getString("password");
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean validateLogin(String name, String password) {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `PLAYERS` WHERE `name`='" + name + "';");
             ResultSet set = statement.executeQuery()) {
            if (set.next()) {
                if (Objects.equals(set.getString("password"), password)) {
                    ChessMenu.IDENTIFIER = set.getString("uuid");
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void setString(String column, String value) {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement("UPDATE `PLAYERS` SET `" + column + "`='" + value + "' WHERE `uuid`='" + ChessMenu.IDENTIFIER + "';")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setNumber(String column, Number value) {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement("UPDATE `PLAYERS` SET `" + column + "`=" + value + " WHERE `uuid`='" + ChessMenu.IDENTIFIER + "';")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void establishConnection(String uuid) {
        try {
            InetAddress address = InetAddress.getByName("127.0.0.1");
            Socket socket = new Socket(address.getHostName(), 55285);

            thread = new DataThread(socket);
            thread.start();

            thread.sendPacket(new Online(uuid));
            thread.sendPacket(new RequestOnline(uuid));

        } catch (ConnectException e) {
            System.out.println("Connection refused: " + e.getCause());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
