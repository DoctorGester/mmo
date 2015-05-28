import shared.board.Board
import shared.board.Buff
import shared.board.ControlType
import shared.board.Unit

def onInit(Buff buff, Board board){
    if (buff.data instanceof Unit) {
        (buff.data as Unit).addBuff(buff)
        (buff.data as Unit).applyControl(ControlType.DISARM, buff)
    } else if (buff.data instanceof List<Unit>){
        for (Unit unit in (buff.data as List<Unit>)){
            unit.addBuff(buff)
            unit.applyControl(ControlType.DISARM, buff)
        }
    }
}

def onTick(Buff buff, Board board){}

def onEnd(Buff buff, Board board){
    if (buff.data instanceof Unit) {
        (buff.data as Unit).removeBuff(buff)
    } else if (buff.data instanceof List<Unit>){

        for (Unit unit in (buff.data as List<Unit>))
            unit.removeBuff(buff)

    }

}